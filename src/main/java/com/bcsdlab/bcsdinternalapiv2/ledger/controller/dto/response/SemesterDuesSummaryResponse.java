package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.SemesterSummary;

public record SemesterDuesSummaryResponse(
        String id,
        int year,
        int term,
        long monthlyAmount,
        int totalMembers,
        int exemptMembers,
        int targetMembers,
        int completedMembers,
        int unpaidMembers,
        long totalAmount,
        long paidAmount,
        long unpaidAmount,
        boolean needsReview
) {

    public static SemesterDuesSummaryResponse of(DuesSemester semester, SemesterSummary summary) {
        return new SemesterDuesSummaryResponse(
                semester.key().id(),
                semester.getYear(),
                semester.getTerm(),
                semester.getMonthlyAmount(),
                summary.totalMembers(),
                summary.exemptMembers(),
                summary.targetMembers(),
                summary.completedMembers(),
                summary.unpaidMembers(),
                summary.totalAmount(),
                summary.paidAmount(),
                summary.unpaidAmount(),
                summary.needsReview());
    }
}
