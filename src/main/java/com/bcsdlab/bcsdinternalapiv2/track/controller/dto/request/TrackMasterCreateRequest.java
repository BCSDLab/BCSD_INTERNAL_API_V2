package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TrackMasterCreateRequest(
        @NotBlank String code,
        @NotBlank String name
) {
}
