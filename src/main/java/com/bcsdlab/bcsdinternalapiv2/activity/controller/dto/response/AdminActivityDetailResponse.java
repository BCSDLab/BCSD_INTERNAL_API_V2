package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;
import java.util.List;

public record AdminActivityDetailResponse(
        Long id,
        Long categoryId,
        Integer year,
        Integer month,
        String title,
        String summary,
        String content,
        String externalUrl,
        boolean isPublished,
        int displayOrder,
        List<String> imageUrls
) {
    public static AdminActivityDetailResponse of(Activity activity, List<String> imageUrls) {
        return new AdminActivityDetailResponse(
                activity.getId(),
                activity.getCategory().getId(),
                activity.getYear(),
                activity.getMonth(),
                activity.getTitle(),
                activity.getSummary(),
                activity.getContent(),
                activity.getExternalUrl(),
                activity.isPublished(),
                activity.getDisplayOrder(),
                imageUrls
        );
    }
}
