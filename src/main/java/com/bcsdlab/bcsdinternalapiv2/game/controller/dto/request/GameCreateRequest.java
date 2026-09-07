package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GameCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 500)
        String oneLiner,

        Long trackId,

        @Size(max = 30)
        String teamLabel
) {
}
