package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrackPageCreateRequest(
        @NotNull Long trackId,

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
