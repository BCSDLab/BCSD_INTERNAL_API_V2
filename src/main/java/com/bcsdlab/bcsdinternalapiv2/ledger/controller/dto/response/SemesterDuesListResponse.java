package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import java.util.List;

public record SemesterDuesListResponse(
        List<SemesterDuesSummaryResponse> semesters
) {
}
