package com.bcsdlab.bcsdinternalapiv2.member.client;

import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class SlackClient {

    private static final String USERS_NOT_FOUND_ERROR = "users_not_found";
    private static final int USERS_LIST_PAGE_LIMIT = 200;
    // 커서가 끝나지 않는 비정상 응답에 대비한 안전장치. 200명 × 50페이지면 워크스페이스 규모를 한참 넘는다.
    private static final int USERS_LIST_MAX_PAGES = 50;

    private final RestClient slackRestClient;

    public Optional<String> findProfileImageUrlByEmail(String email) {
        return lookupByEmail(email).map(user -> profileImageUrl(user.profile()));
    }

    /**
     * 워크스페이스 전체 회원을 users.list(커서 페이지네이션, scope users:read)로 한 번에 읽어
     * Slack 회원 ID → 프로필 이미지 URL 맵을 만든다. 탈퇴(deleted)·봇 계정과 이미지가 없는 회원은 넣지 않는다.
     * 한 페이지라도 실패하면 {@link SlackApiException}이다(429면 Retry-After 포함) — 부분 결과는 돌려주지 않는다.
     */
    public Map<String, String> findAllProfileImageUrlsBySlackId() {
        Map<String, String> imageUrls = new HashMap<>();
        String cursor = null;
        for (int page = 0; page < USERS_LIST_MAX_PAGES; page++) {
            SlackUsersListResponse response = listUsers(cursor);
            if (response.members() != null) {
                for (SlackUsersListResponse.SlackMember member : response.members()) {
                    if (Boolean.TRUE.equals(member.deleted()) || Boolean.TRUE.equals(member.bot())
                            || member.id() == null || member.profile() == null) {
                        continue;
                    }
                    String imageUrl = profileImageUrl(member.profile());
                    if (imageUrl != null) {
                        imageUrls.put(member.id(), imageUrl);
                    }
                }
            }
            cursor = response.responseMetadata() == null ? null : response.responseMetadata().nextCursor();
            if (cursor == null || cursor.isBlank()) {
                return imageUrls;
            }
        }
        throw new SlackApiException("too_many_pages", null);
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

    private SlackUsersListResponse listUsers(String cursor) {
        SlackUsersListResponse response;
        try {
            // 커서는 base64라 '+'가 섞일 수 있다. 템플릿 변수로 넘겨야 %2B로 인코딩된다(그대로 두면 공백으로 읽힌다).
            response = slackRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/users.list").queryParam("limit", USERS_LIST_PAGE_LIMIT);
                        if (cursor == null) {
                            return uriBuilder.build();
                        }
                        return uriBuilder.queryParam("cursor", "{cursor}").build(cursor);
                    })
                    .retrieve()
                    .body(SlackUsersListResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new SlackApiException("ratelimited", retryAfterSeconds(e));
            }
            throw new SlackApiException("http_" + e.getStatusCode().value(), null);
        } catch (RestClientException e) {
            // 연결·읽기 타임아웃 등. 호출부가 SlackApiException 하나만 다루도록 맞춘다.
            throw new SlackApiException("request_failed", null);
        }
        if (response == null) {
            throw new SlackApiException("empty_response", null);
        }
        if (!response.ok()) {
            throw new SlackApiException(response.error(), null);
        }
        return response;
    }

    private static String profileImageUrl(SlackLookupByEmailResponse.SlackProfile profile) {
        return profile.image512() != null ? profile.image512() : profile.image192();
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
