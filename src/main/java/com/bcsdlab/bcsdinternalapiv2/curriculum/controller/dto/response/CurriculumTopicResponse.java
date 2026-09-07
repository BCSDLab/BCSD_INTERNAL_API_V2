package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopic;

public record CurriculumTopicResponse(
        Long id,
        String title,
        int displayOrder
) {
    public static CurriculumTopicResponse from(CurriculumTopic topic) {
        return new CurriculumTopicResponse(topic.getId(), topic.getTitle(), topic.getDisplayOrder());
    }
}
