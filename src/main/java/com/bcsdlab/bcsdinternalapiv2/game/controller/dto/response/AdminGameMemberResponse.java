package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameMember;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

/** 편집 화면(관리자)이 쓰는 참여 멤버 목록. 저트래픽 편집 화면이라 N+1을 감수한다. */
public record AdminGameMemberResponse(
        Long id,
        Long memberId,
        String name,
        String memberType,
        String profileImageUrl,
        int displayOrder
) {
    public static AdminGameMemberResponse from(GameMember gameMember) {
        Member member = gameMember.getMember();
        return new AdminGameMemberResponse(
                gameMember.getId(),
                member.getId(),
                member.getName(),
                member.getMemberType().name(),
                member.getProfileImageUrl(),
                gameMember.getDisplayOrder()
        );
    }
}
