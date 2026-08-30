package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import java.time.Instant;

public record AdminGameBuildResponse(
        Long id,
        String version,
        String status,
        Integer canvasWidth,
        Integer canvasHeight,
        Long storageBytes,
        String buildFileUrl,
        Instant uploadedAt
) {
    public static AdminGameBuildResponse from(GameBuild build) {
        return new AdminGameBuildResponse(
                build.getId(),
                build.getVersion(),
                build.getStatus().name(),
                build.getCanvasWidth(),
                build.getCanvasHeight(),
                build.getStorageBytes(),
                build.getBuildFileUrl(),
                build.getUploadedAt()
        );
    }
}
