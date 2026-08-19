package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;

public record TrackSummaryResponse(
        String slug,
        String name
) {
    public static TrackSummaryResponse from(TrackPage trackPage) {
        return new TrackSummaryResponse(trackPage.getSlug(), trackPage.getDisplayName());
    }
}
