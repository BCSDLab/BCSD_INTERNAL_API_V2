package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.Game;

public record AdminGameSummaryResponse(
        Long id,
        String slug,
        String name,
        boolean isPublished,
        int displayOrder
) {
    public static AdminGameSummaryResponse from(Game game) {
        return new AdminGameSummaryResponse(
                game.getId(), game.getSlug(), game.getName(), game.isPublished(), game.getDisplayOrder());
    }
}
