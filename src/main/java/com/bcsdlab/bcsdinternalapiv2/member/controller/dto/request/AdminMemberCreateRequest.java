package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AdminMemberCreateRequest(
        @NotBlank
        String name,

        @NotBlank
        @Pattern(regexp = "^[0-9]{8,10}$", message = "학번은 숫자 8~10자리여야 합니다.")
        String studentNumber,

        @NotNull
        Track track,

        @NotNull
        MemberType memberType,

        @NotBlank
        String generation,

        @NotBlank
        String university,

        @NotBlank
        @Email
        String email,

        String phoneNumber,

        String githubId
) {
}
