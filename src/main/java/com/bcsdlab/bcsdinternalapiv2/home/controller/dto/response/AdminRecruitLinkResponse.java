package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.home.model.RecruitLink;
import java.time.Instant;
import java.time.LocalDate;

public record AdminRecruitLinkResponse(
        String googleFormUrl,
        boolean isOpen,
        LocalDate closeDate,
        String closedMessage,
        Instant updatedAt
) {
    public static AdminRecruitLinkResponse from(RecruitLink recruitLink) {
        return new AdminRecruitLinkResponse(
                recruitLink.getGoogleFormUrl(), recruitLink.isOpen(), recruitLink.getCloseDate(),
                recruitLink.getClosedMessage(), recruitLink.getUpdatedAt());
    }
}
