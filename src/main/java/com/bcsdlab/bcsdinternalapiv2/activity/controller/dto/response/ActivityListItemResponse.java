package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;
import java.util.List;

public record ActivityListItemResponse(
        Long id,
        int month,
        String title,
        String summary,
        String thumbnailUrl,
        List<String> images,
        String externalUrl,
        boolean hasDetail
) {
    public static ActivityListItemResponse of(Activity activity, List<String> images) {
        return new ActivityListItemResponse(
                activity.getId(),
                activity.getMonth(),
                activity.getTitle(),
                activity.getSummary(),
                images.isEmpty() ? null : images.get(0),
                images,
                activity.getExternalUrl(),
                activity.hasDetail()
        );
    }
}
