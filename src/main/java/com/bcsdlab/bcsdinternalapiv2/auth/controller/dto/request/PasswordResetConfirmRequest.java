package com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetConfirmRequest(
        @NotBlank
        String token,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다.")
        String newPassword,

        @NotBlank
        String newPasswordConfirm
) {
}
