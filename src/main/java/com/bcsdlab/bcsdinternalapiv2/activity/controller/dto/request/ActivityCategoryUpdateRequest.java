package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivityCategoryUpdateRequest(
        @NotBlank
        @Size(max = 30)
        String name,

        @Size(max = 200)
        String headline,

        String heroImageUrl
) {
}
