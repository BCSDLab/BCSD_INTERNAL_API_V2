package com.bcsdlab.bcsdinternalapiv2.member.client;

import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class SlackClient {

    private static final String USERS_NOT_FOUND_ERROR = "users_not_found";

    private final RestClient slackRestClient;

    public Optional<String> findProfileImageUrlByEmail(String email) {
        return lookupByEmail(email).map(user -> {
            SlackLookupByEmailResponse.SlackProfile profile = user.profile();
            return profile.image512() != null ? profile.image512() : profile.image192();
        });
    }

    /** 이메일로 Slack 회원 ID(U…)를 찾는다. 워크스페이스에 없는 이메일이면 empty. */
    public Optional<String> findSlackIdByEmail(String email) {
        return lookupByEmail(email).map(SlackLookupByEmailResponse.SlackUser::id);
    }

    /**
     * 봇이 회원에게 DM을 보낸다. channel에 사용자 ID(U…)를 넣으면 봇과의 DM으로 가서 conversations.open이 필요 없다
     * (scope는 chat:write). 링크 미리보기 카드는 끈다. 실패하면 {@link SlackApiException}이다(429면 Retry-After 포함).
     */
    public SlackPostResult postDirectMessage(String userId, String text) {
        SlackPostMessageResponse response;
        try {
            response = slackRestClient.post()
                    .uri("/chat.postMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("channel", userId, "text", text, "unfurl_links", false, "unfurl_media", false))
                    .retrieve()
                    .body(SlackPostMessageResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new SlackApiException("ratelimited", retryAfterSeconds(e));
            }
            throw new SlackApiException("http_" + e.getStatusCode().value(), null);
        }
        if (response == null) {
            throw new SlackApiException("empty_response", null);
        }
        if (!response.ok()) {
            throw new SlackApiException(response.error(), null);
        }
        return new SlackPostResult(response.channel(), response.ts());
    }

    private static long retryAfterSeconds(RestClientResponseException e) {
        String value = e.getResponseHeaders() == null ? null : e.getResponseHeaders().getFirst("Retry-After");
        try {
            return value == null ? 1 : Math.max(0, Long.parseLong(value.strip()));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private Optional<SlackLookupByEmailResponse.SlackUser> lookupByEmail(String email) {
        SlackLookupByEmailResponse response = slackRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users.lookupByEmail").queryParam("email", email).build())
                .retrieve()
                .body(SlackLookupByEmailResponse.class);

        if (response == null) {
            throw new MemberException(MemberExceptionType.SLACK_SYNC_FAILED);
        }
        if (!response.ok()) {
            if (USERS_NOT_FOUND_ERROR.equals(response.error())) {
                return Optional.empty();
            }
            throw new MemberException(MemberExceptionType.SLACK_SYNC_FAILED
                    .withDetail("slackError=" + response.error()));
        }
        return Optional.of(response.user());
    }
}
