package com.bcsdlab.bcsdinternalapiv2.global.mail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;

final class MailTemplates {

    private MailTemplates() {
    }

    static String load(String templateName) {
        try (InputStream in = new ClassPathResource("mail-templates/" + templateName).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MailDeliveryException("failed to load mail template " + templateName, e);
        }
    }

    static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
