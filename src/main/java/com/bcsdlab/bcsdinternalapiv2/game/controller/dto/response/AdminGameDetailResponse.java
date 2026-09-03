package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import java.util.List;

/**
 * 편집 화면 집계(05-api-spec.md — "기본정보+설명+스크린샷+등급정보+빌드+참여멤버").
 * {@code from}은 헤더만 바꾸는 응답(생성·기본정보 수정·slug 변경)에 쓰고 나머지 목록은
 * 빈 배열/null로 둔다 — 그 호출들은 스크린샷/등급정보/멤버 상태를 프런트가 이미 들고
 * 있어 다시 받을 필요가 없다(AdminTrackPageDetailResponse와 동일한 확장 방식).
 * {@code of}는 전체 편집 화면을 처음 여는 GET에서만 쓴다.
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
        int displayOrder,
        List<GameScreenshotResponse> screenshots,
        GameRatingResponse rating,
        List<AdminGameMemberResponse> members
) {
    public static AdminGameDetailResponse from(Game game) {
        return of(game, List.of(), null, List.of());
    }

    public static AdminGameDetailResponse of(Game game, List<GameScreenshotResponse> screenshots,
                                              GameRatingResponse rating, List<AdminGameMemberResponse> members) {
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
                game.getDisplayOrder(),
                screenshots,
                rating,
                members
        );
    }
}
