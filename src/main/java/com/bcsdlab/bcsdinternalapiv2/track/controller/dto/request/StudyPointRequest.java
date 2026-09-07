package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyPointRequest(
        @NotBlank
        @Size(max = 60)
        String title,

        @NotBlank
        @Size(max = 200)
        String description,

        String iconImageUrl
) {
}
