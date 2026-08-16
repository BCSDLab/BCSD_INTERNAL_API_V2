package com.bcsdlab.bcsdinternalapiv2.auth.security;

import com.bcsdlab.bcsdinternalapiv2.auth.model.TokenScope;
import com.bcsdlab.bcsdinternalapiv2.global.config.JwtProperties;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_STUDENT_NUMBER = "studentNumber";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_SCOPE = "scope";
    private static final String CLAIM_PASSWORD_VERSION = "pwv";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public String createAccessToken(Member member, TokenScope scope, Instant now) {
        long validitySeconds = getValiditySeconds(scope);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bcsd-internal-api")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(validitySeconds))
                .subject(String.valueOf(member.getId()))
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_STUDENT_NUMBER, member.getStudentNumber())
                .claim(CLAIM_ROLE, member.getRole().name())
                .claim(CLAIM_SCOPE, scope.name())
                .claim(CLAIM_PASSWORD_VERSION, passwordVersion(member))
                .build();

        JwsHeader jwsHeader = JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public long getAccessTokenValiditySeconds() {
        return jwtProperties.accessTokenValiditySeconds();
    }

    public long getValiditySeconds(TokenScope scope) {
        return scope == TokenScope.PRE_ACTIVATION
                ? jwtProperties.setupTokenValiditySeconds()
                : jwtProperties.accessTokenValiditySeconds();
    }

    public static long passwordVersion(Member member) {
        return passwordVersion(member.getPasswordChangedAt());
    }

    public static long passwordVersion(Instant passwordChangedAt) {
        return passwordChangedAt != null ? passwordChangedAt.toEpochMilli() : 0L;
    }
}
