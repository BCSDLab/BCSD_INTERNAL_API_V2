package com.bcsdlab.bcsdinternalapiv2.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.reset-password")
public record ResetPasswordProperties(String baseUrl) {
}
