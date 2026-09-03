package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import java.util.List;

public record GameDetailResponse(
        String slug,
        String name,
        String oneLiner,
        String trackName,
        String teamLabel,
        String description,
        String thumbnailUrl,
        List<String> screenshots,
        List<GameMemberResponse> members,
        GameRatingResponse rating,
        GameActiveBuildResponse activeBuild
) {
    public static GameDetailResponse of(Game game, List<String> screenshots, List<GameMemberResponse> members,
                                         GameRatingResponse rating, GameActiveBuildResponse activeBuild) {
        return new GameDetailResponse(
                game.getSlug(),
                game.getName(),
                game.getOneLiner(),
                game.getTrack() != null ? game.getTrack().getName() : null,
                game.getTeamLabel(),
                game.getDescription(),
                game.getThumbnailUrl(),
                screenshots,
                members,
                rating,
                activeBuild
        );
    }
}
