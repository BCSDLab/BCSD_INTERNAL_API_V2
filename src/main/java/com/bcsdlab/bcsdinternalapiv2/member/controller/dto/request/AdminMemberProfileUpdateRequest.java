package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record AdminMemberProfileUpdateRequest(
        @NotBlank
        String name,

        @NotNull
        Track track,

        @NotNull
        MemberType memberType,

        @NotBlank
        String generation,

        @NotBlank
        String university,

        @NotBlank
        String department,

        @NotNull
        List<String> positionCodes,

        LocalDate birthDate,

        boolean duesRequired,

        @NotBlank
        @Email
        String email,

        String phoneNumber,

        String githubId
) {
}
