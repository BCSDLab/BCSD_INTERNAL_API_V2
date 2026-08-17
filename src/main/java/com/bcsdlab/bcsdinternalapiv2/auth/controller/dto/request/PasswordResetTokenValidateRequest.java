package com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetTokenValidateRequest(
        @NotBlank
        String token
) {
}
