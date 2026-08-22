package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
        @NotNull
        MemberRole role
) {
}
