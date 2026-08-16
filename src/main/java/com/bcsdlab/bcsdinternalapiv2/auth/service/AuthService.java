package com.bcsdlab.bcsdinternalapiv2.auth.service;

import com.bcsdlab.bcsdinternalapiv2.auth.model.RefreshToken;
import com.bcsdlab.bcsdinternalapiv2.auth.model.TokenScope;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.LoginRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.LoginResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.TokenResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.security.CookieUtils;
import com.bcsdlab.bcsdinternalapiv2.auth.security.JwtTokenProvider;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthException;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthExceptionType;
import com.bcsdlab.bcsdinternalapiv2.global.util.TokenHasher;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$12$C6UzMDM.H6dfI/f/IKcEeO0y9wJ8W8kQ7pQ0e0m1s8j0J8kP4v0Xy";

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;
    private final LoginAttemptRecorder loginAttemptRecorder;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final RefreshTokenIssuer refreshTokenIssuer;
    private final RefreshTokenRotator refreshTokenRotator;
    private final LoginSessionIssuer loginSessionIssuer;

    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest,
                                HttpServletResponse servletResponse) {
        Instant now = Instant.now();
        Optional<Member> memberOptional = memberRepository.findByStudentNumber(request.studentNumber());

        memberOptional.filter(m -> m.isLocked(now)).ifPresent(m -> {
            throw new AuthException(AuthExceptionType.ACCOUNT_LOCKED);
        });

        String storedPasswordHash = memberOptional.map(Member::getPassword).orElse(DUMMY_PASSWORD_HASH);
        boolean passwordMatches = passwordEncoder.matches(request.password(), storedPasswordHash);

        if (!passwordMatches) {
            memberOptional.ifPresent(m -> loginAttemptRecorder.recordFailedLogin(m.getId(), now));
            log.debug("login failed: studentNumber={}, reason={}", request.studentNumber(),
                    memberOptional.isEmpty() ? "student number not found" : "password mismatch");
            throw new AuthException(AuthExceptionType.INVALID_CREDENTIALS);
        }

        Member member = memberOptional.get();

        if (member.isWithdrawn()) {
            throw new AuthException(AuthExceptionType.ACCOUNT_WITHDRAWN);
        }
        if (!member.isPendingSetup() && !member.isActive()) {
            throw new AuthException(AuthExceptionType.ACCOUNT_LOCKED);
        }

        return loginSessionIssuer.issue(member.getId(), storedPasswordHash, request.rememberMe(), servletRequest,
                servletResponse, now);
    }

    public TokenResponse reissue(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        String rawToken = cookieUtils.readRefreshToken(servletRequest)
                .orElseThrow(() -> new AuthException(AuthExceptionType.REFRESH_TOKEN_INVALID));

        String tokenHash = TokenHasher.sha256Hex(rawToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException(AuthExceptionType.REFRESH_TOKEN_INVALID));

        Instant now = Instant.now();

        if (refreshToken.isExpired(now)) {
            throw new AuthException(AuthExceptionType.REFRESH_TOKEN_EXPIRED);
        }

        Member member = memberRepository.findById(refreshToken.getMemberId())
                .orElseThrow(() -> new AuthException(AuthExceptionType.REFRESH_TOKEN_INVALID));

        if (!member.isActive()) {
            refreshTokenRevoker.revokeAllTokensFor(member.getId(), now);
            cookieUtils.clearRefreshTokenCookie(servletResponse);
            throw new AuthException(AuthExceptionType.REFRESH_TOKEN_INVALID);
        }

        boolean rotated = refreshTokenRotator.rotate(refreshToken.getId(), member.getId(),
                refreshToken.isRememberMe(), servletRequest, servletResponse, now);
        if (!rotated) {
            refreshTokenRevoker.revokeAllTokensFor(refreshToken.getMemberId(), now);
            cookieUtils.clearRefreshTokenCookie(servletResponse);
            log.warn("refresh token reuse detected: memberId={}", refreshToken.getMemberId());
            throw new AuthException(AuthExceptionType.REFRESH_TOKEN_REUSED);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member, TokenScope.FULL, now);
        return TokenResponse.of(accessToken, jwtTokenProvider.getAccessTokenValiditySeconds());
    }

    @Transactional
    public LoginResponse issueFullSessionAfterSetup(Member member, HttpServletRequest servletRequest,
                                                     HttpServletResponse servletResponse) {
        Instant now = Instant.now();
        String accessToken = jwtTokenProvider.createAccessToken(member, TokenScope.FULL, now);
        refreshTokenIssuer.issue(member.getId(), false, servletRequest, servletResponse, null, now);
        return LoginResponse.of(accessToken, jwtTokenProvider.getValiditySeconds(TokenScope.FULL), member);
    }

    @Transactional
    public void logout(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        cookieUtils.readRefreshToken(servletRequest).ifPresent(rawToken -> {
            String tokenHash = TokenHasher.sha256Hex(rawToken);
            refreshTokenRepository.findByTokenHash(tokenHash)
                    .filter(token -> !token.isRevoked())
                    .ifPresent(token -> token.revoke(Instant.now()));
        });
        cookieUtils.clearRefreshTokenCookie(servletResponse);
    }
}
