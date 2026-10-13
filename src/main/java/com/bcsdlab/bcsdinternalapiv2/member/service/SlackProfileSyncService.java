package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.member.client.SlackApiException;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.SlackProfileSyncResponse;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Slack 조회(네트워크 호출)는 트랜잭션 밖에서 수행하고, DB 반영은
 * {@link MemberDirectoryService#updatePhotoUrl}을 외부에서 호출해 프록시를 경유하도록 한다.
 * 같은 클래스 안에서 @Transactional 메서드를 self-invocation으로 호출하면 프록시가
 * 적용되지 않아 변경 사항이 커밋되지 않는다.
 *
 * <p>회원별 lookupByEmail은 회원 수만큼 호출되어 rate limit에 걸리기 쉽고, Apple 비공개 릴레이 이메일로
 * Slack에 가입한 회원은 찾지 못한다. 그래서 users.list로 워크스페이스 전체를 한 번 읽어 slack_id로 매칭하고,
 * slack_id가 없는 회원만 기존 이메일 조회로 보완한다.
 *
 * <p>users.list가 실패하면(429 포함) 동기화 전체를 실패시킨다 — 아무 회원도 갱신하지 않은 상태에서
 * {@link MemberExceptionType#SLACK_SYNC_FAILED}(502)를 던진다. 전원을 이메일 조회로 대체하면 위의
 * rate limit 문제가 그대로 돌아오기 때문이다. 이메일 조회 경로의 429는 해당 회원만 실패로 센다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlackProfileSyncService {

    private final MemberRepository memberRepository;
    private final SlackClient slackClient;
    private final MemberDirectoryService memberDirectoryService;

    public SlackProfileSyncResponse syncAll() {
        // 가입 전·잠김 회원도 홈페이지에 노출될 수 있으므로 탈퇴한 회원만 제외한다.
        List<Member> members = memberRepository.findAllByStatusNot(MemberStatus.WITHDRAWN);
        Map<String, String> imageUrlsBySlackId = fetchImageUrlsBySlackId(members);
        int updated = 0;
        int failed = 0;
        for (Member member : members) {
            try {
                Optional<String> imageUrl = hasSlackId(member)
                        ? Optional.ofNullable(imageUrlsBySlackId.get(member.getSlackId()))
                        : slackClient.findProfileImageUrlByEmail(member.getEmail());
                if (imageUrl.isEmpty()) {
                    throw new IllegalStateException("Slack에서 프로필 이미지를 찾을 수 없습니다.");
                }
                memberDirectoryService.updatePhotoUrl(member.getId(), imageUrl.get());
                updated++;
            } catch (RuntimeException e) {
                failed++;
                log.warn("SLACK_PROFILE_SYNC_FAILED: memberId={}", member.getId(), e);
            }
        }
        return new SlackProfileSyncResponse(members.size(), updated, failed);
    }

    private Map<String, String> fetchImageUrlsBySlackId(List<Member> members) {
        if (members.stream().noneMatch(SlackProfileSyncService::hasSlackId)) {
            return Map.of();
        }
        try {
            return slackClient.findAllProfileImageUrlsBySlackId();
        } catch (SlackApiException e) {
            log.error("SLACK_PROFILE_SYNC_FAILED: users.list slackError={} retryAfterSeconds={}",
                    e.getError(), e.getRetryAfterSeconds());
            throw new MemberException(MemberExceptionType.SLACK_SYNC_FAILED
                    .withDetail("slackError=" + e.getError()
                            + (e.isRateLimited() ? ", retryAfterSeconds=" + e.getRetryAfterSeconds() : "")));
        }
    }

    private static boolean hasSlackId(Member member) {
        return member.getSlackId() != null && !member.getSlackId().isBlank();
    }
}
