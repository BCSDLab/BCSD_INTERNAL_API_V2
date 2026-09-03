package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

public record GameMemberResponse(
        String name,
        String memberType,
        String profileImageUrl
) {
    public static GameMemberResponse from(Member member) {
        return new GameMemberResponse(member.getName(), member.getMemberType().name(), member.getProfileImageUrl());
    }
}
