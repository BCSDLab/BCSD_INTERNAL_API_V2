package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.home.model.QnaItem;

public record QnaResponse(
        String question,
        String answer
) {
    public static QnaResponse from(QnaItem item) {
        return new QnaResponse(item.getQuestion(), item.getAnswer());
    }
}
