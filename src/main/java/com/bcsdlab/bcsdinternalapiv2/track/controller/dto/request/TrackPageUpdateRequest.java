package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrackPageUpdateRequest(
        @NotBlank
        @Size(max = 50)
        String displayName,

        @NotBlank
        @Size(max = 120)
        String tagline,

        String heroImageUrl,
        String ogImageUrl,
        String seoDescription
) {
}
