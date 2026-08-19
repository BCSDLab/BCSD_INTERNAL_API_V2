package com.bcsdlab.bcsdinternalapiv2.global.mail;

import com.bcsdlab.bcsdinternalapiv2.global.config.MailProperties;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "sendmail")
public class SendmailMailSender implements MailSender {

    private static final String MIME_BOUNDARY = "----=_BCSD_Internal_Mail_Boundary";

    private static final String PASSWORD_RESET_HTML_TEMPLATE = MailTemplates.load("password-reset-email.html");
    private static final String ACCOUNT_CREATED_HTML_TEMPLATE = MailTemplates.load("account-creation-email.html");

    private final MailProperties mailProperties;

    @PostConstruct
    void checkSendmailAvailable() {
        if (!new File("/usr/sbin/sendmail").canExecute() && !new File("/usr/bin/sendmail").canExecute()) {
            log.warn("sendmail binary not found at /usr/sbin/sendmail or /usr/bin/sendmail; "
                    + "mail sending will fail until an MTA is installed.");
        }
    }

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
        String message = "From: " + mailProperties.from() + "\r\n"
                + "To: " + toEmail + "\r\n"
                + "Subject: " + subject + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: multipart/alternative; boundary=\"" + MIME_BOUNDARY + "\"\r\n"
                + "\r\n"
                + "--" + MIME_BOUNDARY + "\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "\r\n"
                + text + "\r\n"
                + "\r\n"
                + "--" + MIME_BOUNDARY + "\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "\r\n"
                + html + "\r\n"
                + "\r\n"
                + "--" + MIME_BOUNDARY + "--\r\n";

        ProcessBuilder processBuilder = new ProcessBuilder("sendmail", "-t", "-oi")
                .redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(message.getBytes(StandardCharsets.UTF_8));
            }
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new MailDeliveryException("sendmail timed out sending to " + toEmail);
            }
            if (process.exitValue() != 0) {
                throw new MailDeliveryException(
                        "sendmail exited with code " + process.exitValue() + " sending to " + toEmail);
            }
        } catch (IOException e) {
            throw new MailDeliveryException("failed to invoke sendmail for " + toEmail, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MailDeliveryException("interrupted while sending mail to " + toEmail, e);
        }
    }
}
