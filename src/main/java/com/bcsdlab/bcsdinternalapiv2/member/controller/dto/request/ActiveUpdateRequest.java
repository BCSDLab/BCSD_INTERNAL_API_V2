package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record ActiveUpdateRequest(
        @NotNull
        Boolean active
) {
}
