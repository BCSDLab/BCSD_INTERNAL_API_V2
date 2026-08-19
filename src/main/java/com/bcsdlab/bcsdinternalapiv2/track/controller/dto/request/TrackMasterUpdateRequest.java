package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TrackMasterUpdateRequest(
        @NotBlank String name,
        boolean isActive
) {
}
