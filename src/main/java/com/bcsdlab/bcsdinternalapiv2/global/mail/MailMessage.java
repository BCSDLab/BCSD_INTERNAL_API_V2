package com.bcsdlab.bcsdinternalapiv2.global.mail;

/** 발송 수단과 무관한 메일 내용. 텍스트와 HTML을 함께 보낸다(multipart/alternative). */
record MailMessage(String subject, String text, String html) {
}
