package com.bcsdlab.bcsdinternalapiv2.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "ses")
public class SesEmailClient {

    private final SesClient sesClient;

    @Retryable(retryFor = SdkClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2))
    public void send(SendEmailRequest request, String toEmail) {
        sesClient.sendEmail(request);
    }

    @Recover
    public void recover(SdkException e, SendEmailRequest request, String toEmail) {
        throw new MailDeliveryException("failed to send SES mail to " + toEmail + " after retries", e);
    }
}
