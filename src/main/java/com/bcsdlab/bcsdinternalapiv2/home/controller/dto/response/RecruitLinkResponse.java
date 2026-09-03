package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.home.model.RecruitLink;

public record RecruitLinkResponse(
        String googleFormUrl,
        boolean isOpen,
        String closedMessage
) {
    public static RecruitLinkResponse from(RecruitLink recruitLink) {
        return new RecruitLinkResponse(
                recruitLink.getGoogleFormUrl(), recruitLink.isOpen(), recruitLink.getClosedMessage());
    }
}
