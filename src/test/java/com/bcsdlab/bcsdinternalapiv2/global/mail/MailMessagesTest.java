package com.bcsdlab.bcsdinternalapiv2.global.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.bcsdlab.bcsdinternalapiv2.global.config.MailProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

class MailMessagesTest {

    @Test
    void 비밀번호_재설정_메일() {
        MailMessage mail = MailMessages.passwordReset("https://internal.bcsdlab.com/reset?token=abc");

        assertThat(mail.subject()).isEqualTo("[BCSD Internal] 비밀번호 재설정 안내");
        assertThat(mail.text()).isEqualTo("비밀번호를 재설정하려면 아래 링크를 열어주세요.\n\n"
                + "https://internal.bcsdlab.com/reset?token=abc"
                + "\n\n이 링크는 30분간 유효합니다. 본인이 요청하지 않았다면 이 메일을 무시하세요.");
        assertThat(mail.html()).contains("https://internal.bcsdlab.com/reset?token=abc").doesNotContain("{resetUrl}");
    }

    @Test
    void 계정_생성_메일은_HTML에_임시_비밀번호를_이스케이프한다() {
        MailMessage mail = MailMessages.accountCreated("2024123456", "a<b&c", "https://internal.bcsdlab.com/login");

        assertThat(mail.subject()).isEqualTo("[BCSD Internal] 계정이 생성되었습니다");
        assertThat(mail.text()).isEqualTo("학번: 2024123456\n임시 비밀번호: a<b&c\n로그인: https://internal.bcsdlab.com/login"
                + "\n\n로그인 후 반드시 비밀번호를 변경해주세요.");
        assertThat(mail.html()).contains("2024123456", "a&lt;b&amp;c", "https://internal.bcsdlab.com/login");
    }

    @Test
    void SES_발송기는_만든_내용을_요청에_담는다() {
        SesEmailClient client = mock(SesEmailClient.class);
        MailProperties properties = new MailProperties("ses", "no-reply@bcsdlab.com", "us-west-2");

        new SesMailSender(client, properties).sendPasswordResetLink("user@bcsd.club", "https://x/reset");

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(client).send(captor.capture(), eq("user@bcsd.club"));
        SendEmailRequest request = captor.getValue();
        assertThat(request.source()).isEqualTo("no-reply@bcsdlab.com");
        assertThat(request.destination().toAddresses()).containsExactly("user@bcsd.club");
        assertThat(request.message().subject().data()).isEqualTo("[BCSD Internal] 비밀번호 재설정 안내");
        assertThat(request.message().body().text().data()).contains("https://x/reset");
    }
}
