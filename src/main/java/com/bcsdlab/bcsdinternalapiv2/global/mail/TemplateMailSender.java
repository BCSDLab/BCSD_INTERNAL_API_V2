package com.bcsdlab.bcsdinternalapiv2.global.mail;

/** 메일 내용은 {@link MailMessages}가 만들고, 하위 클래스는 발송 수단(SES, sendmail)만 구현한다. */
abstract class TemplateMailSender implements MailSender {

    @Override
    public void sendPasswordResetLink(String toEmail, String resetUrl) {
        deliver(toEmail, MailMessages.passwordReset(resetUrl));
    }

    @Override
    public void sendAccountCreated(String toEmail, String studentNumber, String temporaryPassword, String loginUrl) {
        deliver(toEmail, MailMessages.accountCreated(studentNumber, temporaryPassword, loginUrl));
    }

    protected abstract void deliver(String toEmail, MailMessage mail);
}
