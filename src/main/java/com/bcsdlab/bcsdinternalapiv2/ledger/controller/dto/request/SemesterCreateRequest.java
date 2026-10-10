package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 월 회비 상한은 오타(0을 더 친 값) 방지용이다. */
public record SemesterCreateRequest(
        @NotNull(message = "년도를 입력하세요.")
        @Min(value = 2000, message = "년도는 2000~2100 사이여야 합니다.")
        @Max(value = 2100, message = "년도는 2000~2100 사이여야 합니다.")
        Integer year,

        @NotNull(message = "학기를 입력하세요.")
        @Min(value = 1, message = "학기는 1 또는 2여야 합니다.")
        @Max(value = 2, message = "학기는 1 또는 2여야 합니다.")
        Integer term,

        @NotNull(message = "월 회비를 입력하세요.")
        @Min(value = 1, message = "월 회비는 1원 이상이어야 합니다.")
        @Max(value = 1_000_000, message = "월 회비는 1,000,000원 이하로 입력하세요.")
        Long monthlyAmount
) {
}
