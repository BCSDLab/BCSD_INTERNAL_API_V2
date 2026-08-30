package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QnaCreateRequest(
        @NotBlank
        @Size(max = 200)
        String question,

        @NotBlank
        String answer
) {
}
