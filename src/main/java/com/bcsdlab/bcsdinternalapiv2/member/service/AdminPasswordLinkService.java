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
 *   <li>DM이 실패하면 전달되지 못한 링크를 사용 처리하고 502로 응답한다.</li>
 *   <li>요청 제한은 검증을 통과한 요청에만 건다. 실패한 검증(Slack ID 누락 등)을 고친 뒤 바로 다시 보낼 수 있게 하기 위해서다.</li>
 *   <li>로그에는 회원 id와 Slack 오류 코드만 남긴다. 링크(원문 토큰)는 남기지 않는다.</li>
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
        if (!rateLimiter.tryAcquire("password-link-slack:" + memberId, RESEND_INTERVAL, Instant.now())) {
            throw new AuthException(AuthExceptionType.TOO_MANY_REQUESTS);
        }

        IssuedResetLink link = passwordResetService.issueSlackLink(memberId, requestedIp);
        try {
            slackClient.postDirectMessage(slackId, message(member, link.url()));
        } catch (SlackApiException e) {
            log.warn("SLACK_DM_FAILED: password link memberId={} tokenId={} slackError={} — 링크를 사용 처리했다",
                    memberId, link.tokenId(), e.getError());
            passwordResetService.revokeToken(link.tokenId());
            throw new MemberException(MemberExceptionType.SLACK_DM_FAILED.withDetail("slackError=" + e.getError()));
        } catch (RuntimeException e) {
            // 연결 실패·타임아웃은 SlackApiException으로 감싸지지 않는다. 링크가 도착했는지 알 수 없으니 똑같이 폐기한다.
            log.warn("SLACK_DM_FAILED: password link memberId={} tokenId={} exception={} — 링크를 사용 처리했다",
                    memberId, link.tokenId(), e.getClass().getSimpleName());
            passwordResetService.revokeToken(link.tokenId());
            throw new MemberException(MemberExceptionType.SLACK_DM_FAILED
                    .withDetail("exception=" + e.getClass().getSimpleName()));
        }
    }

    private static String message(Member member, String url) {
        String purpose = member.isPendingSetup() ? "계정 설정" : "비밀번호 설정";
        return "[BCSD 인터널] %s님, %s 링크입니다.\n%s\n링크는 24시간 동안 한 번만 사용할 수 있습니다. "
                .formatted(member.getName(), purpose, url)
                + "본인이 요청하지 않았다면 운영진에게 알려 주세요.";
    }
}
