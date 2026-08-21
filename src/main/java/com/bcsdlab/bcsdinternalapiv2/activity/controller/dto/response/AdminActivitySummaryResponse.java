package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;

public record AdminActivitySummaryResponse(
        Long id,
        Integer year,
        Integer month,
        String title,
        String summary,
        boolean isPublished,
        int displayOrder
) {
    public static AdminActivitySummaryResponse from(Activity activity) {
        return new AdminActivitySummaryResponse(
                activity.getId(), activity.getYear(), activity.getMonth(), activity.getTitle(),
                activity.getSummary(), activity.isPublished(), activity.getDisplayOrder());
    }
}
