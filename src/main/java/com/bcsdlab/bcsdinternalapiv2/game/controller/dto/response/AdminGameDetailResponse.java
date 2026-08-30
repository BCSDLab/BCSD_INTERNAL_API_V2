package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.Game;

/**
 * 편집 화면의 기본정보·설명 탭 응답. 스크린샷·등급정보·참여멤버·빌드는 존재하지 않는
 * 데이터라 자리표시자를 만들지 않는다 — 해당 태스크(T-38, T-39)가 이 레코드에 필드를
 * 추가하는 방식으로 확장한다(AdminTrackPageDetailResponse와 동일한 확장 방식).
 */
public record AdminGameDetailResponse(
        Long id,
        Long trackId,
        String trackName,
        String slug,
        String name,
        String oneLiner,
        String teamLabel,
        String description,
        String thumbnailUrl,
        boolean isPublished,
        int displayOrder
) {
    public static AdminGameDetailResponse from(Game game) {
        return new AdminGameDetailResponse(
                game.getId(),
                game.getTrack() != null ? game.getTrack().getId() : null,
                game.getTrack() != null ? game.getTrack().getName() : null,
                game.getSlug(),
                game.getName(),
                game.getOneLiner(),
                game.getTeamLabel(),
                game.getDescription(),
                game.getThumbnailUrl(),
                game.isPublished(),
                game.getDisplayOrder()
        );
    }
}
