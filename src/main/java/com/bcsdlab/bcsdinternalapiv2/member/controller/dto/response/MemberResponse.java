package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

public record MemberResponse(
        Long id,
        String name,
        String studentNumber,
        String track,
        String generation,
        String memberType,
        String university,
        String email,
        String phoneNumber,
        String githubId,
        String status
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getStudentNumber(),
                member.getTrack().getCode(),
                member.getGeneration(),
                member.getMemberType().name(),
                member.getUniversity(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getGithubId(),
                member.getStatus().name()
        );
    }
}
