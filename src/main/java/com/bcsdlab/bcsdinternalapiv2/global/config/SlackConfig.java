package com.bcsdlab.bcsdinternalapiv2.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class SlackConfig {

    private static final String SLACK_API_BASE_URL = "https://slack.com/api";
    private static final int CONNECT_TIMEOUT_MILLIS = 3_000;
    private static final int READ_TIMEOUT_MILLIS = 5_000;

    private final SlackProperties slackProperties;

    @Bean
    public RestClient slackRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MILLIS);

        return RestClient.builder()
                .baseUrl(SLACK_API_BASE_URL)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + slackProperties.botToken())
                .build();
    }
}
