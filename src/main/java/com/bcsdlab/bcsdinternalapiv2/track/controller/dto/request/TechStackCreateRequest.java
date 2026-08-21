package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TechStackCreateRequest(
        @NotBlank String name,
        @NotBlank String iconUrl
) {
}
