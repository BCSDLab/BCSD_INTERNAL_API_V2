package com.bcsdlab.bcsdinternalapiv2.auth.service;

import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthException;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthExceptionType;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRotator {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenIssuer refreshTokenIssuer;

    @Transactional
    public boolean rotate(Long oldTokenId, Long memberId, boolean rememberMe, HttpServletRequest servletRequest,
                           HttpServletResponse servletResponse, Instant now) {
        memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new AuthException(AuthExceptionType.REFRESH_TOKEN_INVALID));
        int revokedRows = refreshTokenRepository.revokeIfNotRevoked(oldTokenId, now);
        if (revokedRows == 0) {
            return false;
        }
        refreshTokenIssuer.issue(memberId, rememberMe, servletRequest, servletResponse, oldTokenId, now);
        return true;
    }
}
