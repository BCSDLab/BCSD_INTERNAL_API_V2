package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameScreenshot;

public record GameScreenshotResponse(
        Long id,
        String imageUrl,
        int displayOrder
) {
    public static GameScreenshotResponse from(GameScreenshot screenshot) {
        return new GameScreenshotResponse(screenshot.getId(), screenshot.getImageUrl(), screenshot.getDisplayOrder());
    }
}
