package com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.member.model.Position;

public record PositionResponse(
        String code,
        String name
) {
    public static PositionResponse from(Position position) {
        return new PositionResponse(position.getCode(), position.getName());
    }
}
