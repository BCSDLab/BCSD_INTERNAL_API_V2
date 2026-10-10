package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

/** 학기 명단 한 줄. applicable이 false면 이 학기 납부 비대상이다. */
public record RosterMemberResponse(
        Long memberId,
        String name,
        String studentNumber,
        String track,
        boolean applicable
) {

    public static RosterMemberResponse from(DuesSemesterMember row) {
        Member member = row.getMember();
        return new RosterMemberResponse(
                member.getId(), member.getName(), member.getStudentNumber(), member.getTrack().getCode(),
                row.isApplicable());
    }
}
