package com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response;

public record PresignedUrlResponse(
        Long imageId,
        String uploadUrl,
        String publicUrl
) {
}
