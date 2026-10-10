package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.util.List;

/** 명단에 없는 모든 회원(회원 상태·회비 대상 여부와 무관). 검색은 FE가 클라이언트에서 한다. */
public record RosterCandidateListResponse(
        List<RosterCandidateResponse> members
) {

    public record RosterCandidateResponse(Long memberId, String name, String studentNumber, String track) {

        public static RosterCandidateResponse from(Member member) {
            return new RosterCandidateResponse(
                    member.getId(), member.getName(), member.getStudentNumber(), member.getTrack().getCode());
        }
    }
}
