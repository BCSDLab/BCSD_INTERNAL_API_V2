package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.SlackIdLookupResponse;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.SlackIdLookupResponse.Result;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.SlackIdLookupResponse.Status;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.service.MemberSlackIdWriter.LookupSaveResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * 회원 Slack ID 관리. 이메일 기반 Slack 조회(네트워크)는 트랜잭션 밖에서 하고 DB 반영은
 * {@link MemberSlackIdWriter}에 맡긴다 — 조회 중 DB 커넥션·행 잠금을 잡지 않기 위해서다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberSlackIdService {

    private final MemberRepository memberRepository;
    private final SlackClient slackClient;
    private final MemberSlackIdWriter memberSlackIdWriter;

    public void updateSlackId(Long memberId, String slackId) {
        memberSlackIdWriter.update(memberId, slackId);
    }

    /** 회원마다 결과를 돌려준다 — 한 회원의 실패가 나머지를 막지 않는다. 이미 값이 있는 회원은 Slack을 부르지 않는다. */
    public SlackIdLookupResponse lookupAndSave(List<Long> memberIds) {
        Map<Long, Member> members = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        List<Result> results = memberIds.stream()
                .distinct()
                .map(memberId -> lookupAndSave(memberId, members.get(memberId)))
                .toList();
        return new SlackIdLookupResponse(results);
    }

    /**
     * 부원 추가 직후 호출한다. 결과는 로그로만 남기고 어떤 예외도 밖으로 던지지 않는다 — Slack 조회 때문에
     * 이미 커밋된 회원 생성이 실패 응답이 되면 안 된다.
     */
    public void lookupAndSaveQuietly(Long memberId) {
        try {
            Result result = lookupAndSave(List.of(memberId)).results().getFirst();
            if (result.status() != Status.SAVED) {
                log.info("SLACK_ID_AUTO_LOOKUP_SKIPPED: memberId={}, status={}", memberId, result.status());
            }
        } catch (RuntimeException e) {
            log.warn("SLACK_ID_AUTO_LOOKUP_FAILED: memberId={}", memberId, e);
        }
    }

    private Result lookupAndSave(Long memberId, Member member) {
        if (member == null) {
            return Result.of(memberId, Status.MEMBER_NOT_FOUND);
        }
        if (member.getSlackId() != null) {
            return new Result(memberId, member.getSlackId(), Status.SAVED, null);
        }

        Optional<String> slackId;
        try {
            slackId = slackClient.findSlackIdByEmail(member.getEmail());
        } catch (RuntimeException e) {
            log.warn("SLACK_ID_LOOKUP_FAILED: memberId={}", memberId, e);
            return Result.of(memberId, Status.FAILED);
        }
        if (slackId.isEmpty()) {
            return Result.of(memberId, Status.NOT_FOUND);
        }

        LookupSaveResult saved;
        try {
            saved = memberSlackIdWriter.saveLookedUp(memberId, slackId.get());
        } catch (DataIntegrityViolationException e) {
            // 확인과 커밋 사이에 다른 요청이 같은 값을 저장한 경우(유니크 인덱스 위반).
            return new Result(memberId, slackId.get(), Status.DUPLICATED, null);
        }
        return switch (saved.outcome()) {
            case SAVED -> new Result(memberId, saved.slackId(), Status.SAVED, null);
            case DUPLICATED -> new Result(memberId, slackId.get(), Status.DUPLICATED, saved.ownerName());
            case MEMBER_NOT_FOUND -> Result.of(memberId, Status.MEMBER_NOT_FOUND);
        };
    }
}
