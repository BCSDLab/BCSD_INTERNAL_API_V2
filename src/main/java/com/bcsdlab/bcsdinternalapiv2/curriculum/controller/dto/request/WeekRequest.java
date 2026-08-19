package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 주차 생성/수정 공용. weekTo가 null이면 단일 주차, 있으면 범위(예: 14~17주차)다.
 * weekTo &lt; weekFrom은 서비스 레이어에서 걸러 WEEK_RANGE_INVALID(400)를 던진다 —
 * DB의 ck_week_range도 같은 제약을 갖고 있지만, 더 명확한 메시지를 먼저 준다.
 */
public record WeekRequest(
        @NotNull
        @Min(1)
        @Max(99)
        Integer weekFrom,

        Integer weekTo
) {
}
