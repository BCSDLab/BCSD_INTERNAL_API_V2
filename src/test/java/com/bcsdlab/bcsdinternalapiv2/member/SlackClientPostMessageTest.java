package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.bcsdlab.bcsdinternalapiv2.member.client.SlackApiException;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackPostResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** chat.postMessage 호출 형태와 오류 매핑. 실제 Slack은 부르지 않는다(MockRestServiceServer). */
class SlackClientPostMessageTest {

    private MockRestServiceServer server;
    private SlackClient slackClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://slack.example/api")
                .defaultHeader("Authorization", "Bearer test");
        server = MockRestServiceServer.bindTo(builder).build();
        slackClient = new SlackClient(builder.build());
    }

    @Test
    void 사용자_ID를_channel로_보내고_미리보기를_끈다() {
        server.expect(requestTo("https://slack.example/api/chat.postMessage"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("""
                        {"channel":"U0MEMBER01","text":"안녕","unfurl_links":false,"unfurl_media":false}"""))
                .andRespond(withSuccess("""
                        {"ok":true,"channel":"D0DM000001","ts":"1700000000.000100"}""", MediaType.APPLICATION_JSON));

        SlackPostResult result = slackClient.postDirectMessage("U0MEMBER01", "안녕");

        assertThat(result).isEqualTo(new SlackPostResult("D0DM000001", "1700000000.000100"));
        server.verify();
    }

    @Test
    void ok가_false면_Slack_오류_코드를_담아_던진다() {
        server.expect(requestTo("https://slack.example/api/chat.postMessage"))
                .andRespond(withSuccess("{\"ok\":false,\"error\":\"missing_scope\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> slackClient.postDirectMessage("U0MEMBER01", "안녕"))
                .isInstanceOf(SlackApiException.class)
                .satisfies(e -> {
                    assertThat(((SlackApiException) e).getError()).isEqualTo("missing_scope");
                    assertThat(((SlackApiException) e).isRateLimited()).isFalse();
                });
    }

    @Test
    void 응답이_429면_Retry_After를_담아_던진다() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", "7");
        server.expect(requestTo("https://slack.example/api/chat.postMessage"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).headers(headers));

        assertThatThrownBy(() -> slackClient.postDirectMessage("U0MEMBER01", "안녕"))
                .isInstanceOf(SlackApiException.class)
                .satisfies(e -> {
                    assertThat(((SlackApiException) e).isRateLimited()).isTrue();
                    assertThat(((SlackApiException) e).getRetryAfterSeconds()).isEqualTo(7L);
                });
    }

    @Test
    void 그_밖의_HTTP_오류는_상태_코드를_담아_던진다() {
        server.expect(requestTo("https://slack.example/api/chat.postMessage"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> slackClient.postDirectMessage("U0MEMBER01", "안녕"))
                .isInstanceOf(SlackApiException.class)
                .satisfies(e -> assertThat(((SlackApiException) e).getError()).isEqualTo("http_500"));
    }
}
