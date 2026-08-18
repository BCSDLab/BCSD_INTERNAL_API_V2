package com.bcsdlab.bcsdinternalapiv2.auth.security;

import com.bcsdlab.bcsdinternalapiv2.auth.model.TokenScope;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberStateJwtValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error MEMBER_STATE_MISMATCH =
            new OAuth2Error("invalid_token", "member state mismatch", null);

    private final MemberRepository memberRepository;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Long memberId = parseMemberId(token.getSubject());
        boolean valid = memberId != null && memberRepository.findById(memberId)
                .map(member -> matches(member, token))
                .orElse(false);
        return valid ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(MEMBER_STATE_MISMATCH);
    }

    private boolean matches(Member member, Jwt token) {
        if (member.isWithdrawn()) {
            return false;
        }
        if (JwtTokenProvider.passwordVersion(member) != passwordVersion(token)) {
            return false;
        }
        String scope = token.getClaimAsString("scope");
        if (TokenScope.FULL.name().equals(scope)) {
            return member.isActive();
        }
        if (TokenScope.PRE_ACTIVATION.name().equals(scope)) {
            return member.isPendingSetup();
        }
        return false;
    }

    private Long parseMemberId(String subject) {
        try {
            return subject != null ? Long.valueOf(subject) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private long passwordVersion(Jwt token) {
        Number version = token.getClaim("pwv");
        return version != null ? version.longValue() : 0L;
    }
}
