package com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TrackPageMembersAttachRequest(
        @NotEmpty List<Long> memberIds
) {
}
