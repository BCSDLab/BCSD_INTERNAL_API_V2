package com.bcsdlab.bcsdinternalapiv2.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(String transport, String from, String sesRegion) {
}
