package com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String status,
        MemberSummary member
) {

    public record MemberSummary(
            Long id,
            String name,
            String studentNumber,
            String track,
            String generation,
            String memberType,
            String university,
            String role
    ) {
        public static MemberSummary from(Member member) {
            return new MemberSummary(
                    member.getId(),
                    member.getName(),
                    member.getStudentNumber(),
                    member.getTrack().getCode(),
                    member.getGeneration(),
                    member.getMemberType().name(),
                    member.getUniversity(),
                    member.getRole().name()
            );
        }
    }

    public static LoginResponse of(String accessToken, long expiresIn, Member member) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresIn,
                member.getStatus().name(),
                MemberSummary.from(member)
        );
    }
}
