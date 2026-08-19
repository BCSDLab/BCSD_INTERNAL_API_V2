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
public class SesMailSender implements MailSender {

    private static final String PASSWORD_RESET_HTML_TEMPLATE = MailTemplates.load("password-reset-email.html");
    private static final String ACCOUNT_CREATED_HTML_TEMPLATE = MailTemplates.load("account-creation-email.html");

    private final SesEmailClient sesEmailClient;
    private final MailProperties mailProperties;

    @Override
    public void sendPasswordResetLink(String toEmail, String resetUrl) {
        String subject = "[BCSD Internal] 비밀번호 재설정 안내";
        String text = "비밀번호를 재설정하려면 아래 링크를 열어주세요.\n\n" + resetUrl
                + "\n\n이 링크는 30분간 유효합니다. 본인이 요청하지 않았다면 이 메일을 무시하세요.";
        String html = PASSWORD_RESET_HTML_TEMPLATE.replace("{resetUrl}", resetUrl);
        send(toEmail, subject, text, html);
    }

    @Override
    public void sendAccountCreated(String toEmail, String studentNumber, String temporaryPassword, String loginUrl) {
        String subject = "[BCSD Internal] 계정이 생성되었습니다";
        String text = "학번: " + studentNumber
                + "\n임시 비밀번호: " + temporaryPassword
                + "\n로그인: " + loginUrl
                + "\n\n로그인 후 반드시 비밀번호를 변경해주세요.";
        String html = ACCOUNT_CREATED_HTML_TEMPLATE
                .replace("{studentNumber}", MailTemplates.escapeHtml(studentNumber))
                .replace("{temporaryPassword}", MailTemplates.escapeHtml(temporaryPassword))
                .replace("{loginUrl}", loginUrl);
        send(toEmail, subject, text, html);
    }

    private void send(String toEmail, String subject, String text, String html) {
        SendEmailRequest request = SendEmailRequest.builder()
                .source(mailProperties.from())
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                        .subject(Content.builder().charset("UTF-8").data(subject).build())
                        .body(Body.builder()
                                .text(Content.builder().charset("UTF-8").data(text).build())
                                .html(Content.builder().charset("UTF-8").data(html).build())
                                .build())
                        .build())
                .build();

        sesEmailClient.send(request, toEmail);
    }
}
