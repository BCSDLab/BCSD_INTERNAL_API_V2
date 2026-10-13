package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthException;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthExceptionType;
import com.bcsdlab.bcsdinternalapiv2.auth.service.PasswordResetService;
import com.bcsdlab.bcsdinternalapiv2.auth.service.PasswordResetService.IssuedResetLink;
import com.bcsdlab.bcsdinternalapiv2.global.util.SimpleRateLimiter;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackApiException;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 이메일을 받을 수 없는 회원에게 관리자가 비밀번호 설정 링크를 Slack DM으로 보낸다. 기존 비밀번호 재설정 토큰을 그대로 쓴다.
 *
 * <ul>
 *   <li>트랜잭션을 걸지 않는다. 토큰 발급은 {@link PasswordResetService}의 트랜잭션에서 커밋하고, Slack 호출은 그 뒤
 *       트랜잭션 밖에서 동기로 한다 — 관리자가 발송 실패를 바로 알아야 해서 AFTER_COMMIT 비동기 발송을 쓰지 않는다.</li>
 *   <li>DM이 실패하면 전달되지 못한 링크를 사용 처리하고 502로 응답한다. 요청 제한도 풀어 바로 다시 보낼 수 있다.</li>
 *   <li>요청 제한은 검증을 통과한 요청에만 건다. 실패한 검증(Slack ID 누락 등)을 고친 뒤 바로 다시 보낼 수 있게 하기 위해서다.</li>
 *   <li>로그에는 회원 id와 Slack 오류 코드만 남긴다. 링크(원문 토큰)는 남기지 않는다. 응답에는 Slack 오류 원문을 넣지 않는다.</li>
 *   <li>보내면 토큰의 mail_sent_at(전달 시각)을 남긴다.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPasswordLinkService {

    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(1);

    private final MemberRepository memberRepository;
    private final PasswordResetService passwordResetService;
    private final SlackClient slackClient;
    private final SimpleRateLimiter rateLimiter;

    public void sendViaSlack(Long memberId, String requestedIp) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));
        if (member.isWithdrawn()) {
            throw new MemberException(MemberExceptionType.MEMBER_WITHDRAWN);
        }
        String slackId = member.getSlackId();
        if (slackId == null || slackId.isBlank()) {
            throw new MemberException(MemberExceptionType.SLACK_ID_MISSING);
        }
        String rateLimitKey = "password-link-slack:" + memberId;
        if (!rateLimiter.tryAcquire(rateLimitKey, RESEND_INTERVAL, Instant.now())) {
            throw new AuthException(AuthExceptionType.TOO_MANY_REQUESTS);
        }

        IssuedResetLink link;
        try {
            link = passwordResetService.issueSlackLink(memberId, requestedIp);
            deliver(member, slackId, link);
        } catch (RuntimeException e) {
            // 보내지 못했으면 1분 제한을 풀어 원인을 고친 뒤 바로 다시 보낼 수 있게 한다.
            rateLimiter.release(rateLimitKey);
            throw e;
        }

        // DM은 이미 갔다. 시각 기록이 실패해도 관리자에게 실패로 보이지 않게 로그만 남긴다.
        try {
            passwordResetService.markSlackLinkSent(link.tokenId());
        } catch (RuntimeException e) {
            log.warn("PASSWORD_LINK_SENT_AT_NOT_RECORDED: memberId={} tokenId={}", memberId, link.tokenId(), e);
        }
    }

    private void deliver(Member member, String slackId, IssuedResetLink link) {
        try {
            slackClient.postDirectMessage(slackId, message(member, link.url()));
        } catch (SlackApiException e) {
            log.warn("SLACK_DM_FAILED: password link memberId={} tokenId={} slackError={} — 링크를 사용 처리했다",
                    member.getId(), link.tokenId(), e.getError());
            passwordResetService.revokeToken(link.tokenId());
            // Slack 오류 원문은 로그에만 남기고, 화면에는 관리자가 할 일을 알려 주는 문구만 보낸다.
            throw new MemberException(dmFailure(e.getError()));
        } catch (RuntimeException e) {
            // 연결 실패·타임아웃은 SlackApiException으로 감싸지지 않는다. 링크가 도착했는지 알 수 없으니 똑같이 폐기한다.
            log.warn("SLACK_DM_FAILED: password link memberId={} tokenId={} exception={} — 링크를 사용 처리했다",
                    member.getId(), link.tokenId(), e.getClass().getSimpleName());
            passwordResetService.revokeToken(link.tokenId());
            throw new MemberException(MemberExceptionType.SLACK_DM_FAILED);
        }
    }

    /** chat.postMessage 오류 코드(docs.slack.dev/reference/methods/chat.postMessage)를 관리자 안내 문구로 바꾼다. */
    private static MemberExceptionType dmFailure(String slackError) {
        return switch (slackError == null ? "" : slackError) {
            // DM 채널을 열 수 없을 때도 channel_not_found가 온다(잘못된 ID, 워크스페이스를 떠난 사용자).
            case "channel_not_found", "user_not_found", "user_disabled" -> MemberExceptionType.SLACK_DM_USER_UNREACHABLE;
            case "messages_tab_disabled" -> MemberExceptionType.SLACK_DM_APP_UNAVAILABLE;
            case "ratelimited", "rate_limited" -> MemberExceptionType.SLACK_DM_RATE_LIMITED;
            default -> MemberExceptionType.SLACK_DM_FAILED;
        };
    }

    private static String message(Member member, String url) {
        String purpose = member.isPendingSetup() ? "계정 설정" : "비밀번호 설정";
        return "[BCSD 인터널] %s님, %s 링크입니다.\n%s\n링크는 24시간 동안 한 번만 사용할 수 있습니다. "
                .formatted(member.getName(), purpose, url)
                + "본인이 요청하지 않았다면 운영진에게 알려 주세요.";
    }
}
