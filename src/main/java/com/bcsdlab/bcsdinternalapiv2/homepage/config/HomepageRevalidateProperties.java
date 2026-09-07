package com.bcsdlab.bcsdinternalapiv2.homepage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.homepage-revalidate")
public record HomepageRevalidateProperties(String url, String secret) {
}
