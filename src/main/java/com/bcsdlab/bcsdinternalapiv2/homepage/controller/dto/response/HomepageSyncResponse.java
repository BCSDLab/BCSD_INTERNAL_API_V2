package com.bcsdlab.bcsdinternalapiv2.homepage.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.homepage.model.HomepageSyncStatus;
import java.time.Instant;
import java.util.List;

public record HomepageSyncResponse(
        Instant lastSucceededAt,
        Instant lastFailedAt,
        List<String> pendingTags
) {
    public static HomepageSyncResponse from(HomepageSyncStatus status) {
        return new HomepageSyncResponse(status.getLastSucceededAt(), status.getLastFailedAt(),
                status.getPendingTagList());
    }
}
