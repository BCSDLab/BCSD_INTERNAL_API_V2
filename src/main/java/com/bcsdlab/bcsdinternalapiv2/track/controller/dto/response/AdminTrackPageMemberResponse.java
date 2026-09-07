package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPageMember;

/**
 * 편집 화면(관리자)이 쓰는 배정 목록. 저트래픽 편집 화면이라 부원 조회의 N+1을
 * 감수한다(T-11 관리자 커리큘럼 트리와 같은 이유).
 */
public record AdminTrackPageMemberResponse(
        Long id,
        Long memberId,
        String name,
        String memberType,
        String profileImageUrl,
        boolean isVisible,
        int displayOrder
) {
    public static AdminTrackPageMemberResponse from(TrackPageMember trackPageMember) {
        Member member = trackPageMember.getMember();
        return new AdminTrackPageMemberResponse(
                trackPageMember.getId(),
                member.getId(),
                member.getName(),
                member.getMemberType().name(),
                member.getProfileImageUrl(),
                trackPageMember.isVisible(),
                trackPageMember.getDisplayOrder()
        );
    }
}
