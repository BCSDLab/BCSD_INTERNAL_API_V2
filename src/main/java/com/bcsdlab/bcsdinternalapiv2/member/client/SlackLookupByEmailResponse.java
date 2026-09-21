package com.bcsdlab.bcsdinternalapiv2.member.client;

import com.fasterxml.jackson.annotation.JsonProperty;

record SlackLookupByEmailResponse(boolean ok, String error, SlackUser user) {

    record SlackUser(String id, SlackProfile profile) {
    }

    record SlackProfile(
            @JsonProperty("image_192") String image192,
            @JsonProperty("image_512") String image512
    ) {
    }
}
