package com.bcsdlab.bcsdinternalapiv2.auth.service;

import com.bcsdlab.bcsdinternalapiv2.auth.model.PasswordResetToken;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.ResetTokenValidationResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.PasswordResetTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthException;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthExceptionType;
import com.bcsdlab.bcsdinternalapiv2.global.config.ResetPasswordProperties;
import com.bcsdlab.bcsdinternalapiv2.global.util.SimpleRateLimiter;
import com.bcsdlab.bcsdinternalapiv2.global.util.TokenHasher;
import com.bcsdlab.bcsdinternalapiv2.global.mail.MailDeliveryException;
import com.bcsdlab.bcsdinternalapiv2.global.mail.MailSender;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Duration TOKEN_VALIDITY = Duration.ofMinutes(30);
    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(5);
    private static final Duration IP_RESEND_INTERVAL = Duration.ofSeconds(3);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResetPasswordProperties resetPasswordProperties;
    private final SimpleRateLimiter rateLimiter;
    private final ApplicationEventPublisher eventPublisher;
    private final MailSender mailSender;

    @Transactional
    public void requestReset(String rawEmail, String requestedIp) {
        String email = rawEmail.trim().toLowerCase();
        Instant now = Instant.now();

        if (!rateLimiter.tryAcquire("password-reset-ip:" + requestedIp, IP_RESEND_INTERVAL, now)) {
            throw new AuthException(AuthExceptionType.TOO_MANY_REQUESTS);
        }
        if (!rateLimiter.tryAcquire("password-reset:" + email, RESEND_INTERVAL, now)) {
            throw new AuthException(AuthExceptionType.TOO_MANY_REQUESTS);
        }

        memberRepository.findByEmail(email)
                .flatMap(member -> memberRepository.findByIdForUpdate(member.getId()))
                .ifPresent(member -> {
                    passwordResetTokenRepository.markAllUnusedAsUsed(member.getId(), now);

                    String rawToken = generateOpaqueToken();
                    PasswordResetToken token = PasswordResetToken.builder()
                            .memberId(member.getId())
                            .tokenHash(TokenHasher.sha256Hex(rawToken))
                            .expiresAt(now.plus(TOKEN_VALIDITY))
                            .requestedIp(requestedIp)
                            .createdAt(now)
                            .build();
                    passwordResetTokenRepository.save(token);

                    String resetUrl = resetPasswordProperties.baseUrl() + "?token=" + rawToken;
                    eventPublisher.publishEvent(
                            new PasswordResetRequestedEvent(token.getId(), member.getEmail(), resetUrl));
                });
    }

    @Transactional(readOnly = true)
    public ResetTokenValidationResponse validateToken(String rawToken) {
        Member member = resolveValidToken(rawToken).member();
        return new ResetTokenValidationResponse(true, maskStudentNumber(member.getStudentNumber()));
    }

    @Transactional
    public void confirmReset(String rawToken, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new AuthException(AuthExceptionType.PASSWORD_CONFIRM_MISMATCH);
        }

        ValidTokenResult result = resolveValidToken(rawToken);
        Instant now = Instant.now();

        Member member = memberRepository.findByIdForUpdate(result.member().getId())
                .orElseThrow(() -> new AuthException(AuthExceptionType.RESET_TOKEN_INVALID));

        int updated = passwordResetTokenRepository.markUsedIfUnused(result.token().getId(), now);
        if (updated != 1) {
            throw new AuthException(AuthExceptionType.RESET_TOKEN_ALREADY_USED);
        }

        member.resetPassword(passwordEncoder.encode(newPassword), now);
        passwordResetTokenRepository.markAllUnusedAsUsed(member.getId(), now);

        refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(member.getId())
                .forEach(refreshToken -> refreshToken.revoke(now));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendResetMailAfterCommit(PasswordResetRequestedEvent event) {
        try {
            mailSender.sendPasswordResetLink(event.email(), event.resetUrl());
        } catch (MailDeliveryException e) {
            log.error("MAIL_DELIVERY_FAILED: password reset link to={} tokenId={} — token is already committed; "
                    + "mail_sent_at stays null so this token is identifiable for a manual resend.",
                    event.email(), event.tokenId(), e);
            return;
        }
        passwordResetTokenRepository.markMailSent(event.tokenId(), Instant.now());
    }

    private ValidTokenResult resolveValidToken(String rawToken) {
        String tokenHash = TokenHasher.sha256Hex(rawToken);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException(AuthExceptionType.RESET_TOKEN_INVALID));

        Instant now = Instant.now();
        if (token.isUsed()) {
            throw new AuthException(AuthExceptionType.RESET_TOKEN_ALREADY_USED);
        }
        if (token.isExpired(now)) {
            throw new AuthException(AuthExceptionType.RESET_TOKEN_EXPIRED);
        }

        Member member = memberRepository.findById(token.getMemberId())
                .orElseThrow(() -> new AuthException(AuthExceptionType.RESET_TOKEN_INVALID));

        return new ValidTokenResult(token, member);
    }

    private record ValidTokenResult(PasswordResetToken token, Member member) {
    }

    private String maskStudentNumber(String studentNumber) {
        return studentNumber.substring(0, 4) + "****";
    }

    private String generateOpaqueToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
