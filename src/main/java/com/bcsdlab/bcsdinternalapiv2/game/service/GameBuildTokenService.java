package com.bcsdlab.bcsdinternalapiv2.game.service;

import com.bcsdlab.bcsdinternalapiv2.game.config.GameBuildProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 업로드 토큰 발급(ADR-024). 검증은 홈페이지 서버(별도 코드베이스, T-41b)가
 * 같은 시크릿으로 HMAC을 다시 계산해 수행한다 — 이 레포는 발급만 한다.
 */
@Service
@RequiredArgsConstructor
public class GameBuildTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final GameBuildProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record IssuedToken(String token, Instant expiresAt) {
    }

    private record Payload(Long buildId, Long gameId, String slug, String version, long exp) {
    }

    public IssuedToken issue(Long buildId, Long gameId, String slug, String version) {
        Instant expiresAt = Instant.now().plusSeconds(properties.tokenValiditySeconds());
        Payload payload = new Payload(buildId, gameId, slug, version, expiresAt.getEpochSecond());
        byte[] payloadBytes = writeValueAsBytes(payload);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadBytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(hmac(payloadBytes));
        return new IssuedToken(encodedPayload + "." + signature, expiresAt);
    }

    private byte[] hmac(byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] writeValueAsBytes(Payload payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
