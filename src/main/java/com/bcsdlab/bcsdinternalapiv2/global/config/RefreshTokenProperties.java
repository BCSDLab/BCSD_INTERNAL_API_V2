package com.bcsdlab.bcsdinternalapiv2.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.refresh-token")
public record RefreshTokenProperties(
        String cookieName,
        boolean cookieSecure,
        long defaultValidityDays,
        long rememberMeValidityDays
) {
}
