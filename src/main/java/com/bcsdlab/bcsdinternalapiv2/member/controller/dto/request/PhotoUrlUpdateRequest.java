package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PhotoUrlUpdateRequest(
        @NotBlank
        String photoUrl
) {
}
