package com.bcsdlab.bcsdinternalapiv2.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.model.PasswordResetToken;
import com.bcsdlab.bcsdinternalapiv2.auth.model.RefreshToken;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.PasswordResetTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.schedule.ExpiredTokenCleanupScheduler;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.repository.TrackMasterRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ExpiredTokenCleanupIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ExpiredTokenCleanupScheduler scheduler;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Test
    void 만료된_지_30일이_지난_토큰만_지운다() {
        Long memberId = memberRepository.save(Member.builder()
                .studentNumber("20400001")
                .name("토큰정리")
                .track(trackMasterRepository.findByCode("BACKEND").orElseThrow())
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .email("cleanup@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.MEMBER)
                .build()).getId();
        Instant now = Instant.now();
        refreshToken(memberId, "old", now.minus(Duration.ofDays(31)));
        refreshToken(memberId, "recently-expired", now.minus(Duration.ofDays(1)));
        refreshToken(memberId, "valid", now.plus(Duration.ofDays(1)));
        resetToken(memberId, "old", now.minus(Duration.ofDays(31)));
        resetToken(memberId, "valid", now.plus(Duration.ofMinutes(30)));

        scheduler.deleteExpiredTokens();

        assertThat(refreshTokenRepository.findAll()).extracting(RefreshToken::getTokenHash)
                .containsExactlyInAnyOrder("recently-expired", "valid");
        assertThat(passwordResetTokenRepository.findAll()).extracting(PasswordResetToken::getTokenHash)
                .containsExactly("valid");
    }

    private void refreshToken(Long memberId, String hash, Instant expiresAt) {
        refreshTokenRepository.save(RefreshToken.builder()
                .memberId(memberId).tokenHash(hash).expiresAt(expiresAt)
                .createdAt(expiresAt.minus(Duration.ofDays(1)))
                .build());
    }

    private void resetToken(Long memberId, String hash, Instant expiresAt) {
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .memberId(memberId).tokenHash(hash).expiresAt(expiresAt).requestedIp("127.0.0.1")
                .createdAt(expiresAt.minus(Duration.ofMinutes(30)))
                .build());
    }
}
