package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;

public record ActivityCategoryResponse(
        String slug,
        String name,
        String headline,
        String heroImageUrl
) {
    public static ActivityCategoryResponse from(ActivityCategory category) {
        return new ActivityCategoryResponse(
                category.getSlug(), category.getName(), category.getHeadline(), category.getHeroImageUrl());
    }
}
