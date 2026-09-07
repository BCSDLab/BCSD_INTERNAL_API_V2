package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.home.model.RecruitLinkHistory;
import java.time.Instant;

public record RecruitLinkHistoryResponse(
        String googleFormUrl,
        boolean isOpen,
        String changedByName,
        Instant changedAt
) {
    public static RecruitLinkHistoryResponse from(RecruitLinkHistory history) {
        return new RecruitLinkHistoryResponse(
                history.getGoogleFormUrl(),
                history.isOpen(),
                history.getChangedBy() != null ? history.getChangedBy().getName() : null,
                history.getChangedAt()
        );
    }
}
