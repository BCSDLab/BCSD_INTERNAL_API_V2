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

        // 임시 비밀번호인 회원만 필수다(서비스에서 검사). 비밀번호 설정 링크로 이미 정한 회원은 비워 보낸다.
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다.")
        String newPassword,

        String newPasswordConfirm
) {
}
