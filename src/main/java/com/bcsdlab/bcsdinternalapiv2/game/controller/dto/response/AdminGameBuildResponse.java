package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import com.bcsdlab.bcsdinternalapiv2.game.util.GameBuildUrlNormalizer;
import java.time.Instant;

public record AdminGameBuildResponse(
        Long id,
        String version,
        String status,
        Integer canvasWidth,
        Integer canvasHeight,
        Long storageBytes,
        String buildFileUrl,
        String failureReason,
        Instant uploadedAt
) {
    public static AdminGameBuildResponse from(GameBuild build, String publicOrigin) {
        return new AdminGameBuildResponse(
                build.getId(),
                build.getVersion(),
                build.getStatus().name(),
                build.getCanvasWidth(),
                build.getCanvasHeight(),
                build.getStorageBytes(),
                GameBuildUrlNormalizer.normalize(build.getBuildFileUrl(), publicOrigin),
                build.getFailureReason(),
                build.getUploadedAt()
        );
    }
}
