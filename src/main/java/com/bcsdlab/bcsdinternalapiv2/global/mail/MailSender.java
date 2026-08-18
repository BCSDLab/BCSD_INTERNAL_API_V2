package com.bcsdlab.bcsdinternalapiv2.global.mail;

public interface MailSender {

    void sendPasswordResetLink(String toEmail, String resetUrl);

    void sendAccountCreated(String toEmail, String studentNumber, String temporaryPassword, String loginUrl);
}
