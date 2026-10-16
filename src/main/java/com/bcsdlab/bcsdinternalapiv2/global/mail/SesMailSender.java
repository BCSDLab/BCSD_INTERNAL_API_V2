package com.bcsdlab.bcsdinternalapiv2.global.mail;

import com.bcsdlab.bcsdinternalapiv2.global.config.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "ses")
public class SesMailSender extends TemplateMailSender {

    private final SesEmailClient sesEmailClient;
    private final MailProperties mailProperties;

    @Override
    protected void deliver(String toEmail, MailMessage mail) {
        SendEmailRequest request = SendEmailRequest.builder()
                .source(mailProperties.from())
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().charset("UTF-8").data(mail.subject()).build())
                        .body(Body.builder()
                                .text(Content.builder().charset("UTF-8").data(mail.text()).build())
                                .html(Content.builder().charset("UTF-8").data(mail.html()).build())
                                .build())
                        .build())
                .build();

        sesEmailClient.send(request, toEmail);
    }
}
