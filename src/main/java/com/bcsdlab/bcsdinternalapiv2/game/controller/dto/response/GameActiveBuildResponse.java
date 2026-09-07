package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import com.bcsdlab.bcsdinternalapiv2.game.util.GameBuildUrlNormalizer;

/**
 * 이번 1차에서는 {@code buildFileUrl}이 항상 null일 수 있다 — 실제 파일 저장은 후속
 * 태스크(T-41, ADR-023)에서 다룬다(FR-7.6).
 */
public record GameActiveBuildResponse(
        String version,
        String status,
        String buildFileUrl,
        Integer canvasWidth,
        Integer canvasHeight
) {
    public static GameActiveBuildResponse from(GameBuild gameBuild, String publicOrigin) {
        return new GameActiveBuildResponse(
                gameBuild.getVersion(),
                gameBuild.getStatus().name(),
                GameBuildUrlNormalizer.normalize(gameBuild.getBuildFileUrl(), publicOrigin),
                gameBuild.getCanvasWidth(),
                gameBuild.getCanvasHeight()
        );
    }
}
