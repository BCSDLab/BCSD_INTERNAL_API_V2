package com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import java.time.Instant;

public record ImageAssetResponse(
        Long id,
        String url,
        String originalName,
        Instant createdAt
) {
    public static ImageAssetResponse from(ImageAsset imageAsset) {
        return new ImageAssetResponse(
                imageAsset.getId(), imageAsset.getUrl(), imageAsset.getOriginalName(), imageAsset.getCreatedAt());
    }
}
