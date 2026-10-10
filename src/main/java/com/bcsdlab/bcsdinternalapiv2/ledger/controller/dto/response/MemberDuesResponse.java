package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterDuesStatus;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.MemberDues;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.util.List;

/** 납부 비대상이면 assessedAmount·paidAmount·unpaidAmount가 null이고 excessAmount는 0이다. */
public record MemberDuesResponse(
        Long memberId,
        String name,
        String studentNumber,
        String track,
        String slackId,
        List<MonthDuesResponse> months,
        SemesterDuesStatus status,
        Long assessedAmount,
        Long paidAmount,
        Long unpaidAmount,
        long excessAmount
) {

    public static MemberDuesResponse of(Member member, MemberDues dues) {
        return new MemberDuesResponse(
                member.getId(),
                member.getName(),
                member.getStudentNumber(),
                member.getTrack().getCode(),
                member.getSlackId(),
                dues.months().stream().map(MonthDuesResponse::from).toList(),
                dues.status(),
                dues.assessedAmount(),
                dues.paidAmount(),
                dues.unpaidAmount(),
                dues.excessAmount());
    }
}
