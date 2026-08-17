package com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9]{8,10}$", message = "학번은 숫자 8~10자리여야 합니다.")
        String studentNumber,

        @NotBlank
        String password,

        boolean rememberMe
) {
}
