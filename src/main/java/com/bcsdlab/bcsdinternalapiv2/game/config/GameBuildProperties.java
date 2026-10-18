package com.bcsdlab.bcsdinternalapiv2.game.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code secret}은 홈페이지 서버와 공유하는 값으로, 완료 웹훅 인증과 업로드 토큰 서명에 함께 쓰인다.
 * 저장소에 기본값을 두지 않는다 — 비어 있으면 웹훅과 토큰 발급을 모두 막는다(fail-closed).
 */
@ConfigurationProperties(prefix = "app.game-build")
public record GameBuildProperties(String uploadUrl, String secret, long tokenValiditySeconds, String publicOrigin) {

    public boolean isSecretConfigured() {
        return secret != null && !secret.isBlank();
    }

    /** 길이와 내용이 응답 시간으로 드러나지 않도록 해시 후 상수 시간으로 비교한다. */
    public boolean matchesSecret(String presented) {
        if (!isSecretConfigured() || presented == null || presented.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(sha256(secret), sha256(presented));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
