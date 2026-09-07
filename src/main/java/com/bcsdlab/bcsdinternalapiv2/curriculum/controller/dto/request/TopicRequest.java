package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicRequest(
        @NotBlank
        @Size(max = 200)
        String title
) {
}
