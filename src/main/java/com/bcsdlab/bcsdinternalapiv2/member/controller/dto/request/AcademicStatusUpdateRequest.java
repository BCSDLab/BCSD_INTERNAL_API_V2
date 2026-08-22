package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import jakarta.validation.constraints.NotNull;

public record AcademicStatusUpdateRequest(
        @NotNull
        AcademicStatus academicStatus
) {
}
