package com.bcsdlab.bcsdinternalapiv2.game.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.game-build")
public record GameBuildProperties(String uploadUrl, String secret, long tokenValiditySeconds) {
}
