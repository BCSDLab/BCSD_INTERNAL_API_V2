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
public class SendmailMailSender extends TemplateMailSender {

    private static final String MIME_BOUNDARY = "----=_BCSD_Internal_Mail_Boundary";

    private final MailProperties mailProperties;

    @PostConstruct
    void checkSendmailAvailable() {
        if (!new File("/usr/sbin/sendmail").canExecute() && !new File("/usr/bin/sendmail").canExecute()) {
            log.warn("sendmail binary not found at /usr/sbin/sendmail or /usr/bin/sendmail; "
                    + "mail sending will fail until an MTA is installed.");
        }
    }

    @Override
    protected void deliver(String toEmail, MailMessage mail) {
        String subject = mail.subject();
        String text = mail.text();
        String html = mail.html();
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
