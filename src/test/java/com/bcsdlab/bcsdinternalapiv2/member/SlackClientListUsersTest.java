package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.bcsdlab.bcsdinternalapiv2.member.client.SlackApiException;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** users.list 페이지네이션과 필터링·오류 매핑. 실제 Slack은 부르지 않는다(MockRestServiceServer). */
class SlackClientListUsersTest {

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
    void 커서를_따라_모든_페이지를_읽고_탈퇴_봇_계정은_뺀다() {
        server.expect(requestTo("https://slack.example/api/users.list?limit=200"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"ok":true,"cache_ts":1700000000,
                         "members":[
                           {"id":"U0AAAAAAA01","name":"a","deleted":false,"is_bot":false,
                            "profile":{"image_192":"https://img/a192.png","image_512":"https://img/a512.png","email":"a@x"}},
                           {"id":"U0AAAAAAA02","deleted":false,"is_bot":false,
                            "profile":{"image_192":"https://img/b192.png"}},
                           {"id":"U0GONE00001","deleted":true,"profile":{"image_512":"https://img/gone.png"}},
                           {"id":"B0BOT000001","deleted":false,"is_bot":true,"profile":{"image_512":"https://img/bot.png"}}
                         ],
                         "response_metadata":{"next_cursor":"dXNlcjpVMEc5V0ZYTlo+/="}}""",
                        MediaType.APPLICATION_JSON));
        // base64 커서의 '+', '/', '='는 퍼센트 인코딩되어야 Slack이 그대로 읽는다.
        server.expect(requestTo("https://slack.example/api/users.list?limit=200&cursor=dXNlcjpVMEc5V0ZYTlo%2B%2F%3D"))
                .andRespond(withSuccess("""
                        {"ok":true,
                         "members":[{"id":"U0AAAAAAA03","profile":{"image_512":"https://img/c512.png"}},
                                    {"id":"U0NOIMAGE01","profile":{}}],
                         "response_metadata":{"next_cursor":""}}""", MediaType.APPLICATION_JSON));

        Map<String, String> result = slackClient.findAllProfileImageUrlsBySlackId();

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                "U0AAAAAAA01", "https://img/a512.png",
                "U0AAAAAAA02", "https://img/b192.png",
                "U0AAAAAAA03", "https://img/c512.png"));
        server.verify();
    }

    @Test
    void ok가_false면_Slack_오류_코드를_담아_던진다() {
        server.expect(requestTo("https://slack.example/api/users.list?limit=200"))
                .andRespond(withSuccess("{\"ok\":false,\"error\":\"missing_scope\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> slackClient.findAllProfileImageUrlsBySlackId())
                .isInstanceOf(SlackApiException.class)
                .satisfies(e -> assertThat(((SlackApiException) e).getError()).isEqualTo("missing_scope"));
    }

    @Test
    void 두번째_페이지가_429면_Retry_After를_담아_던진다() {
        server.expect(requestTo("https://slack.example/api/users.list?limit=200"))
                .andRespond(withSuccess("""
                        {"ok":true,"members":[],"response_metadata":{"next_cursor":"next"}}""",
                        MediaType.APPLICATION_JSON));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", "30");
        server.expect(requestTo("https://slack.example/api/users.list?limit=200&cursor=next"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).headers(headers));

        assertThatThrownBy(() -> slackClient.findAllProfileImageUrlsBySlackId())
                .isInstanceOf(SlackApiException.class)
                .satisfies(e -> {
                    assertThat(((SlackApiException) e).isRateLimited()).isTrue();
                    assertThat(((SlackApiException) e).getRetryAfterSeconds()).isEqualTo(30L);
                });
    }
}
