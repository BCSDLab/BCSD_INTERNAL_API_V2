package com.bcsdlab.bcsdinternalapiv2.auth.service;

import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.LoginResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthException;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthExceptionType;
import com.bcsdlab.bcsdinternalapiv2.auth.model.TokenScope;
import com.bcsdlab.bcsdinternalapiv2.auth.security.CookieUtils;
import com.bcsdlab.bcsdinternalapiv2.auth.security.JwtTokenProvider;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginSessionIssuer {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;
    private final RefreshTokenIssuer refreshTokenIssuer;

    @Transactional
    public LoginResponse issue(Long memberId, String verifiedPasswordHash, boolean rememberMe,
                                HttpServletRequest servletRequest, HttpServletResponse servletResponse, Instant now) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new AuthException(AuthExceptionType.INVALID_CREDENTIALS));

        if (!member.getPassword().equals(verifiedPasswordHash)) {
            throw new AuthException(AuthExceptionType.INVALID_CREDENTIALS);
        }
        if (member.isWithdrawn()) {
            throw new AuthException(AuthExceptionType.ACCOUNT_WITHDRAWN);
        }
        if (!member.isPendingSetup() && !member.isActive()) {
            throw new AuthException(AuthExceptionType.ACCOUNT_LOCKED);
        }

        member.recordSuccessfulLogin(now);

        TokenScope scope = member.isPendingSetup() ? TokenScope.PRE_ACTIVATION : TokenScope.FULL;
        String accessToken = jwtTokenProvider.createAccessToken(member, scope, now);

        if (scope == TokenScope.FULL) {
            refreshTokenIssuer.issue(member.getId(), rememberMe, servletRequest, servletResponse, null, now);
        } else {
            cookieUtils.clearRefreshTokenCookie(servletResponse);
        }

        return LoginResponse.of(accessToken, jwtTokenProvider.getValiditySeconds(scope), member);
    }
}
