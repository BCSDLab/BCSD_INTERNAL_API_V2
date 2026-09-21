package com.bcsdlab.bcsdinternalapiv2.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class SlackConfig {

    private static final String SLACK_API_BASE_URL = "https://slack.com/api";

    private final SlackProperties slackProperties;

    @Bean
    public RestClient slackRestClient() {
        return RestClient.builder()
                .baseUrl(SLACK_API_BASE_URL)
                .defaultHeader("Authorization", "Bearer " + slackProperties.botToken())
                .build();
    }
}
