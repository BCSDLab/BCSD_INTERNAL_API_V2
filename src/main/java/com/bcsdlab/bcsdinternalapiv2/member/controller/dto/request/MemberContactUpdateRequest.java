package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberContactUpdateRequest(
        @NotBlank
        String phoneNumber,

        @NotBlank
        @Email
        String email,

        String githubId
) {
}
