package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
        String department,

        AcademicStatus academicStatus,

        Boolean active,

        @NotBlank
        @Email
        String email,

        String phoneNumber,

        String githubId,

        @Size(max = 100)
        String company,

        // false면 로그인 계정 없이 명부에만 등록한다(비밀번호 없음, 환영 메일 없음). 생략하면 기존처럼 계정을 만든다.
        Boolean createAccount
) {
    public boolean shouldCreateAccount() {
        return createAccount == null || createAccount;
    }
}
