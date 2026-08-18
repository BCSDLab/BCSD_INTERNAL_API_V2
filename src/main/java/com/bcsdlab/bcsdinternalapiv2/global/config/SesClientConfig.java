package com.bcsdlab.bcsdinternalapiv2.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "ses")
public class SesClientConfig {

    private final MailProperties mailProperties;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(mailProperties.sesRegion()))
                .build();
    }
}
