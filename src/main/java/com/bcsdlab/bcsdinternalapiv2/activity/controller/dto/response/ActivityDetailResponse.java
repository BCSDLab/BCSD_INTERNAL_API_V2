package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;
import java.util.List;

public record ActivityDetailResponse(
        Long id,
        String categorySlug,
        int year,
        int month,
        String title,
        String summary,
        String content,
        List<String> images,
        String externalUrl
) {
    public static ActivityDetailResponse of(Activity activity, List<String> images) {
        return new ActivityDetailResponse(
                activity.getId(),
                activity.getCategory().getSlug(),
                activity.getYear(),
                activity.getMonth(),
                activity.getTitle(),
                activity.getSummary(),
                activity.getContent(),
                images,
                activity.getExternalUrl()
        );
    }
}
