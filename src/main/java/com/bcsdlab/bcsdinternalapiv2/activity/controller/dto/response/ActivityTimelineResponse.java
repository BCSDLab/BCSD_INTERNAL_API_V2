package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response;

import java.util.List;

public record ActivityTimelineResponse(
        int year,
        List<ActivityListItemResponse> activities
) {
}
