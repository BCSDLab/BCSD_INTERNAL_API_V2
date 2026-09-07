package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;

public record TrackMasterResponse(
        Long id,
        String code,
        String name,
        boolean isActive,
        boolean hasTrackPage
) {
    public static TrackMasterResponse of(TrackMaster track, boolean hasTrackPage) {
        return new TrackMasterResponse(track.getId(), track.getCode(), track.getName(), track.isActive(),
                hasTrackPage);
    }
}
