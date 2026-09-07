package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;

/**
 * 세트 목록용 요약. 주차·토픽 수는 아직 없다 — 그 개념이 생기는 T-11에서 이 레코드에
 * 필드를 추가한다.
 */
public record AdminCurriculumSummaryResponse(
        Long id,
        String name,
        boolean isPublished,
        int displayOrder
) {
    public static AdminCurriculumSummaryResponse from(Curriculum curriculum) {
        return new AdminCurriculumSummaryResponse(
                curriculum.getId(), curriculum.getName(), curriculum.isPublished(), curriculum.getDisplayOrder());
    }
}
