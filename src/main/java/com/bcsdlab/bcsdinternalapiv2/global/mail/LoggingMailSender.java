package com.bcsdlab.bcsdinternalapiv2.global.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "log")
public class LoggingMailSender implements MailSender {

    @Override
    public void sendPasswordResetLink(String toEmail, String resetUrl) {
        log.info("[MAIL] password reset link sent to={}", toEmail);
    }

    @Override
    public void sendAccountCreated(String toEmail, String studentNumber, String temporaryPassword, String loginUrl) {
        log.info("[MAIL] account created notice sent to={} studentNumber={}", toEmail, studentNumber);
    }
}
