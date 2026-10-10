package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import java.util.List;

public record SemesterDuesDetailResponse(
        SemesterDuesSummaryResponse semester,
        List<MemberDuesResponse> members
) {
}
