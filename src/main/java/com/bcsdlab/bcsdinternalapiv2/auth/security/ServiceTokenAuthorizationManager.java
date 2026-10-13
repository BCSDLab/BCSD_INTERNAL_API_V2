package com.bcsdlab.bcsdinternalapiv2.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * {@code Authorization: Bearer <token>}을 설정된 서비스 토큰과 비교한다. 서비스 전용 필터 체인에만 붙인다 —
 * 빈으로 등록하지 않으므로 다른 경로에는 영향이 없다.
 *
 * <p>설정 토큰이 비어 있으면 무조건 거부한다(빈 토큰끼리 일치하는 것을 막는다). 시크릿 끝의 개행 같은
 * 공백은 양쪽 모두 제거하고 비교한다. 길이가 드러나지 않도록
 * 양쪽을 SHA-256으로 고정 길이로 만든 뒤 {@link MessageDigest#isEqual}로 상수 시간 비교한다.
 * 거부되면 익명 요청이므로 {@link JwtAuthenticationEntryPoint}가 401을 응답한다.
 */
public class ServiceTokenAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final String BEARER_PREFIX = "Bearer ";

    private final byte[] expectedDigest;

    public ServiceTokenAuthorizationManager(String configuredToken) {
        this.expectedDigest = configuredToken == null || configuredToken.isBlank() ? null : sha256(configuredToken.strip());
    }

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication,
                                           RequestAuthorizationContext context) {
        return new AuthorizationDecision(matches(context.getRequest().getHeader(HttpHeaders.AUTHORIZATION)));
    }

    private boolean matches(String authorizationHeader) {
        if (expectedDigest == null || authorizationHeader == null
                || !authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return false;
        }
        String presented = authorizationHeader.substring(BEARER_PREFIX.length()).strip();
        if (presented.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(expectedDigest, sha256(presented));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
