package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record MentorSlotCreateRequest(
        @NotNull Long memberId
) {
}
