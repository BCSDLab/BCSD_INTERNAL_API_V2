package com.bcsdlab.bcsdinternalapiv2.auth.service;

import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRevoker {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllTokensFor(Long memberId, Instant now) {
        refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(memberId)
                .forEach(token -> token.revoke(now));
    }
}
