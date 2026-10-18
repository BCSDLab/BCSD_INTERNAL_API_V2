package com.bcsdlab.bcsdinternalapiv2.auth.schedule;

import com.bcsdlab.bcsdinternalapiv2.auth.repository.PasswordResetTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 refresh token·비밀번호 재설정 토큰을 지운다. 로그인과 토큰 갱신마다 행이 하나씩 쌓여 지워지지 않았다.
 *
 * <p>만료된 토큰은 갱신 시 재사용 검사보다 먼저 "만료"로 거절되므로 지워도 재사용 감지에는 영향이 없다.
 * 다만 만료 직후 지우면 만료된 쿠키로 요청한 사용자가 "만료" 대신 "유효하지 않음" 안내를 받으므로
 * {@link #RETENTION}만큼 여유를 둔다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredTokenCleanupScheduler {

    /** 로그인 유지(remember-me) 유효 기간과 같다. */
    static final Duration RETENTION = Duration.ofDays(30);

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Clock clock;

    // 매일 04:00(KST). 사용자가 적은 시간대다.
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void deleteExpiredTokens() {
        Instant cutoff = Instant.now(clock).minus(RETENTION);
        int refreshTokens = refreshTokenRepository.deleteExpiredBefore(cutoff);
        int resetTokens = passwordResetTokenRepository.deleteExpiredBefore(cutoff);
        log.info("EXPIRED_TOKEN_CLEANUP_DONE: refreshTokens={} passwordResetTokens={} cutoff={}",
                refreshTokens, resetTokens, cutoff);
    }
}
