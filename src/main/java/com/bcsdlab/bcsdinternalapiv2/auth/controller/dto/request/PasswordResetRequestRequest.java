package com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestRequest(
        @NotBlank
        @Email
        String email
) {
}
