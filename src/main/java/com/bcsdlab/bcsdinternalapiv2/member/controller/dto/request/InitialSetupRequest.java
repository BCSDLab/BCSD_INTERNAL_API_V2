package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InitialSetupRequest(
        @NotBlank
        String phoneNumber,

        @NotBlank
        @Email
        String email,

        String githubId,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다.")
        String newPassword,

        @NotBlank
        String newPasswordConfirm
) {
}
