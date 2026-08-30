package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.home.model.QnaItem;

public record AdminQnaResponse(
        Long id,
        String question,
        String answer,
        boolean isPublished,
        int displayOrder
) {
    public static AdminQnaResponse from(QnaItem item) {
        return new AdminQnaResponse(
                item.getId(), item.getQuestion(), item.getAnswer(), item.isPublished(), item.getDisplayOrder());
    }
}
