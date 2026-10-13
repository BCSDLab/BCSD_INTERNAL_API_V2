package com.bcsdlab.bcsdinternalapiv2.member.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * users.list 응답. 다음 페이지가 없으면 response_metadata.next_cursor가 빈 문자열이다.
 * deleted·is_bot은 빠질 수 있어 래퍼 타입이다(Jackson 3은 null을 원시 타입에 넣지 않는다).
 */
record SlackUsersListResponse(
        boolean ok,
        String error,
        List<SlackMember> members,
        @JsonProperty("response_metadata") ResponseMetadata responseMetadata
) {

    record SlackMember(
            String id,
            Boolean deleted,
            @JsonProperty("is_bot") Boolean bot,
            SlackLookupByEmailResponse.SlackProfile profile
    ) {
    }

    record ResponseMetadata(@JsonProperty("next_cursor") String nextCursor) {
    }
}
