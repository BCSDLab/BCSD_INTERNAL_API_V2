package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;
import java.util.List;

/**
 * 커리큘럼 편집 화면 — 주차 &gt; 토픽 &gt; 세부항목 3단 트리 전체.
 * 편집 화면(운영진 소수, 낮은 트래픽) 전용이라 N+1을 감수한다 — 공개 API(T-12)는
 * 트래픽 특성이 달라 join fetch/배치 조회로 따로 최적화한다.
 */
public record AdminCurriculumTreeResponse(
        Long id,
        String name,
        boolean isPublished,
        List<WeekNode> weeks
) {
    public static AdminCurriculumTreeResponse of(Curriculum curriculum, List<WeekNode> weeks) {
        return new AdminCurriculumTreeResponse(curriculum.getId(), curriculum.getName(), curriculum.isPublished(),
                weeks);
    }

    public record WeekNode(
            Long id,
            Integer weekFrom,
            Integer weekTo,
            int displayOrder,
            List<TopicNode> topics
    ) {
    }

    public record TopicNode(
            Long id,
            String title,
            int displayOrder,
            List<String> details
    ) {
    }
}
