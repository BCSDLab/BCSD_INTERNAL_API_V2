package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.time.LocalDate;

public record MemberSummaryResponse(
        Long id,
        String name,
        String generation,
        String track,
        String memberType,
        String academicStatus,
        String university,
        String department,
        String position,
        LocalDate birthDate,
        boolean duesRequired,
        String studentNumber,
        String email,
        String phoneNumber,
        String githubId,
        String photoUrl,
        String role,
        boolean active
) {
    public static MemberSummaryResponse from(Member member) {
        return new MemberSummaryResponse(
                member.getId(),
                member.getName(),
                member.getGeneration(),
                member.getTrack().getCode(),
                member.getMemberType().name(),
                member.getAcademicStatus().name(),
                member.getUniversity(),
                member.getDepartment(),
                member.getPosition(),
                member.getBirthDate(),
                member.isDuesRequired(),
                member.getStudentNumber(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getGithubId(),
                member.getProfileImageUrl(),
                member.getRole().name(),
                member.isClubActive()
        );
    }
}
