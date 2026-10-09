package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SlackIdLookupRequest(
        @NotEmpty
        List<@NotNull Long> memberIds
) {
}
