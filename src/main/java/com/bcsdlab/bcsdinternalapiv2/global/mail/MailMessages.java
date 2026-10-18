package com.bcsdlab.bcsdinternalapiv2.global.mail;

/** 서비스가 보내는 메일의 제목·본문을 만든다. */
final class MailMessages {

    private static final String PASSWORD_RESET_HTML_TEMPLATE = MailTemplates.load("password-reset-email.html");
    private static final String ACCOUNT_CREATED_HTML_TEMPLATE = MailTemplates.load("account-creation-email.html");

    private MailMessages() {
    }

    static MailMessage passwordReset(String resetUrl) {
        String subject = "[BCSD Internal] 비밀번호 재설정 안내";
        String text = "비밀번호를 재설정하려면 아래 링크를 열어주세요.\n\n" + resetUrl
                + "\n\n이 링크는 30분간 유효합니다. 본인이 요청하지 않았다면 이 메일을 무시하세요.";
        String html = PASSWORD_RESET_HTML_TEMPLATE.replace("{resetUrl}", resetUrl);
        return new MailMessage(subject, text, html);
    }

    static MailMessage accountCreated(String studentNumber, String temporaryPassword, String loginUrl) {
        String subject = "[BCSD Internal] 계정이 생성되었습니다";
        String text = "학번: " + studentNumber
                + "\n임시 비밀번호: " + temporaryPassword
                + "\n로그인: " + loginUrl
                + "\n\n로그인 후 반드시 비밀번호를 변경해주세요.";
        String html = ACCOUNT_CREATED_HTML_TEMPLATE
                .replace("{studentNumber}", MailTemplates.escapeHtml(studentNumber))
                .replace("{temporaryPassword}", MailTemplates.escapeHtml(temporaryPassword))
                .replace("{loginUrl}", loginUrl);
        return new MailMessage(subject, text, html);
    }
}
