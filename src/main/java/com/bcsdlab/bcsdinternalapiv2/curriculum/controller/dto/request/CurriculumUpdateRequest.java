package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CurriculumUpdateRequest(
        @NotBlank
        @Size(max = 50)
        String name
) {
}
