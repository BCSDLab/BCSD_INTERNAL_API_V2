package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Slack ID DB 반영 전용 빈. {@link MemberSlackIdService}가 Slack 호출(네트워크)을 트랜잭션 밖에서 한 뒤
 * 이 빈을 외부 호출해 프록시를 거치게 한다 — {@link SlackProfileSyncService}와 같은 구조.
 */
@Service
@RequiredArgsConstructor
public class MemberSlackIdWriter {

    private final MemberRepository memberRepository;

    /** 관리자 직접 입력. 다른 회원이 쓰는 값이면 409(사용 중인 회원 이름 포함). null이면 지운다. */
    @Transactional
    public void update(Long memberId, String slackId) {
        Member member = findForUpdate(memberId);
        if (slackId != null) {
            findOtherOwner(slackId, memberId).ifPresent(owner -> {
                throw duplicated(owner);
            });
        }
        member.updateSlackId(slackId);
        try {
            // 사전 확인과 저장 사이에 다른 요청이 같은 값을 넣은 경우 — 유니크 인덱스 위반을 409로 바꾼다
            // (전역 핸들러는 DataIntegrityViolationException을 400으로 응답한다).
            memberRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new MemberException(MemberExceptionType.SLACK_ID_DUPLICATED);
        }
    }

    /**
     * Slack 조회로 찾은 값을 저장한다. 실패시키지 않고 결과만 돌려준다 — 그 사이 값이 채워졌으면 덮어쓰지
     * 않고, 다른 회원이 쓰는 값이면 저장하지 않는다.
     */
    @Transactional
    public LookupSaveResult saveLookedUp(Long memberId, String slackId) {
        Optional<Member> found = memberRepository.findByIdForUpdate(memberId);
        if (found.isEmpty()) {
            return LookupSaveResult.memberNotFound();
        }
        Member member = found.get();
        if (member.getSlackId() != null) {
            return LookupSaveResult.saved(member.getSlackId());
        }
        Optional<Member> owner = findOtherOwner(slackId, memberId);
        if (owner.isPresent()) {
            return LookupSaveResult.duplicated(owner.get().getName());
        }
        member.updateSlackId(slackId);
        return LookupSaveResult.saved(slackId);
    }

    private Member findForUpdate(Long memberId) {
        return memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));
    }

    private Optional<Member> findOtherOwner(String slackId, Long memberId) {
        return memberRepository.findBySlackId(slackId).filter(owner -> !owner.getId().equals(memberId));
    }

    private MemberException duplicated(Member owner) {
        return new MemberException(new SlackIdDuplicatedType(owner.getName()));
    }

    /**
     * 관리자 화면에 그대로 띄우는 문장이라 withDetail(디버그용 "-> detail:" 형식) 대신 사용 중인 회원 이름을
     * 넣은 메시지를 만든다. 관리자 전용 API라 이름 노출은 문제없다.
     */
    private record SlackIdDuplicatedType(String ownerName) implements BcsdExceptionType {

        @Override
        public HttpStatus getHttpStatus() {
            return MemberExceptionType.SLACK_ID_DUPLICATED.getHttpStatus();
        }

        @Override
        public String getMessage() {
            return "이미 %s 회원이 사용 중인 Slack ID입니다.".formatted(ownerName);
        }

        @Override
        public BcsdExceptionType withDetail(String detailMessage) {
            return MemberExceptionType.SLACK_ID_DUPLICATED.withDetail(detailMessage);
        }
    }

    public record LookupSaveResult(Outcome outcome, String slackId, String ownerName) {

        enum Outcome { SAVED, DUPLICATED, MEMBER_NOT_FOUND }

        static LookupSaveResult saved(String slackId) {
            return new LookupSaveResult(Outcome.SAVED, slackId, null);
        }

        static LookupSaveResult duplicated(String ownerName) {
            return new LookupSaveResult(Outcome.DUPLICATED, null, ownerName);
        }

        static LookupSaveResult memberNotFound() {
            return new LookupSaveResult(Outcome.MEMBER_NOT_FOUND, null, null);
        }
    }
}
