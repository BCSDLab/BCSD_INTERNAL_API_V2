package com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;

/**
 * 활동 건수(activity 잔존 여부 판단에 쓰는 그 수)는 아직 없다 — Activity가 생기는
 * T-15에서 이 레코드에 필드를 추가한다.
 */
public record AdminActivityCategoryResponse(
        Long id,
        String slug,
        String name,
        String headline,
        String heroImageUrl,
        boolean isPublished,
        int displayOrder
) {
    public static AdminActivityCategoryResponse from(ActivityCategory category) {
        return new AdminActivityCategoryResponse(
                category.getId(), category.getSlug(), category.getName(), category.getHeadline(),
                category.getHeroImageUrl(), category.isPublished(), category.getDisplayOrder());
    }
}
