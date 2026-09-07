package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

public record MentorResponse(
        String name,
        String trackName,
        String profileImageUrl
) {
    public static MentorResponse from(Member member) {
        return new MentorResponse(member.getName(), member.getTrack().getName(), member.getProfileImageUrl());
    }
}
