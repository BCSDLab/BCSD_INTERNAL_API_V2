package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;

/**
 * 내부 서비스(bbot)에 내려주는 회원 한 명. 연락처·학번·생년월일·소속 등 개인정보와 비밀번호 관련 필드는
 * 의도적으로 넣지 않는다 — 필드를 추가할 때는 bbot에 정말 필요한지부터 따진다.
 */
public record ServiceMemberResponse(
        Long id,
        String name,
        String slackId,
        String githubId,
        String track,
        MemberType memberType,
        boolean clubActive,
        AcademicStatus academicStatus
) {

    public static ServiceMemberResponse from(Member member) {
        return new ServiceMemberResponse(
                member.getId(),
                member.getName(),
                member.getSlackId(),
                member.getGithubId(),
                member.getTrack() != null ? member.getTrack().getCode() : null,
                member.getMemberType(),
                member.isClubActive(),
                member.getAcademicStatus()
        );
    }
}
