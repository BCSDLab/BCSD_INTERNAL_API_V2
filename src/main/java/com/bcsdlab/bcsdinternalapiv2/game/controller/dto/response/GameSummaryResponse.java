package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.Game;

public record GameSummaryResponse(
        String slug,
        String name,
        String oneLiner,
        String thumbnailUrl
) {
    public static GameSummaryResponse from(Game game) {
        return new GameSummaryResponse(game.getSlug(), game.getName(), game.getOneLiner(), game.getThumbnailUrl());
    }
}
