package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

public record TrackMemberResponse(
        String name,
        String memberType,
        String profileImageUrl
) {
    public static TrackMemberResponse from(Member member) {
        return new TrackMemberResponse(member.getName(), member.getMemberType().name(), member.getProfileImageUrl());
    }
}
