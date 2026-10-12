package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import io.swagger.v3.oas.annotations.media.Schema;

/** 본인 정보(GET /v1/members/me, /v1/members/me/initial-setup). slackId는 인명부에 없으면 null이다. */
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
        String status,
        String role,
        String photoUrl,
        @Schema(description = "인명부 Slack 멤버 ID. 없으면 null", example = "U0ABCDEFGH1", nullable = true)
        String slackId
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
                member.getStatus().name(),
                member.getRole().name(),
                member.getProfileImageUrl(),
                member.getSlackId()
        );
    }
}
