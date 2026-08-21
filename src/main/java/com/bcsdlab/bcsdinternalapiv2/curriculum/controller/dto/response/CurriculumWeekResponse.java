package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumWeek;

public record CurriculumWeekResponse(
        Long id,
        Integer weekFrom,
        Integer weekTo,
        int displayOrder
) {
    public static CurriculumWeekResponse from(CurriculumWeek week) {
        return new CurriculumWeekResponse(week.getId(), week.getWeekFrom(), week.getWeekTo(), week.getDisplayOrder());
    }
}
