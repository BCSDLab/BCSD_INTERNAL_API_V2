package com.bcsdlab.bcsdinternalapiv2.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 내부 서비스(사람이 아닌 클라이언트)가 {@code /v1/service/**}를 호출할 때 쓰는 고정 토큰.
 * 비어 있으면 해당 서비스의 요청은 전부 거부된다(fail closed).
 */
@ConfigurationProperties(prefix = "app.service")
public record ServiceTokenProperties(String bbotToken) {
}
