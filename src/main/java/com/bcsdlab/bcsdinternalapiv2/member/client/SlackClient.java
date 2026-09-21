package com.bcsdlab.bcsdinternalapiv2.member.client;

import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SlackClient {

    private static final String USERS_NOT_FOUND_ERROR = "users_not_found";

    private final RestClient slackRestClient;

    public Optional<String> findProfileImageUrlByEmail(String email) {
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

        SlackLookupByEmailResponse.SlackProfile profile = response.user().profile();
        String imageUrl = profile.image512() != null ? profile.image512() : profile.image192();
        return Optional.ofNullable(imageUrl);
    }
}
