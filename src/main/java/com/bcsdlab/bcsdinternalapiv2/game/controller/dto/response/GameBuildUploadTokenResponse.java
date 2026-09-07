package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response;

import java.time.Instant;

public record GameBuildUploadTokenResponse(
        String uploadUrl,
        String token,
        Instant expiresAt
) {
}
