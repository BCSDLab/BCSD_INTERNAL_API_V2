package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesLink;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

public record DuesLinkResponse(
        Long memberId,
        String memberName,
        String studentNumber,
        String track,
        String semesterId
) {

    public static DuesLinkResponse of(DuesLink link, Member member, SemesterKey semester) {
        return new DuesLinkResponse(
                link.getMemberId(), member.getName(), member.getStudentNumber(), member.getTrack().getCode(),
                semester.id());
    }
}
