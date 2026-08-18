package com.bcsdlab.bcsdinternalapiv2.auth.service;

import com.bcsdlab.bcsdinternalapiv2.auth.model.RefreshToken;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.security.CookieUtils;
import com.bcsdlab.bcsdinternalapiv2.global.config.RefreshTokenProperties;
import com.bcsdlab.bcsdinternalapiv2.global.util.TokenHasher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefreshTokenIssuer {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtils cookieUtils;
    private final RefreshTokenProperties refreshTokenProperties;

    @Transactional
    public void issue(Long memberId, boolean rememberMe, HttpServletRequest servletRequest,
                       HttpServletResponse servletResponse, Long rotatedFromId, Instant now) {
        long validityDays = rememberMe
                ? refreshTokenProperties.rememberMeValidityDays()
                : refreshTokenProperties.defaultValidityDays();

        String rawToken = generateOpaqueToken();
        String tokenHash = TokenHasher.sha256Hex(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(memberId)
                .tokenHash(tokenHash)
                .expiresAt(now.plus(validityDays, ChronoUnit.DAYS))
                .rememberMe(rememberMe)
                .rotatedFromId(rotatedFromId)
                .userAgent(servletRequest.getHeader("User-Agent"))
                .createdAt(now)
                .build();
        refreshTokenRepository.save(refreshToken);

        long maxAgeSeconds = ChronoUnit.SECONDS.between(now, refreshToken.getExpiresAt());
        cookieUtils.addRefreshTokenCookie(servletResponse, rawToken, maxAgeSeconds);
    }

    private String generateOpaqueToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
