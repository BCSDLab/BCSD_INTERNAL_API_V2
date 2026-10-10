package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 월 회비 상한은 오타(0을 더 친 값) 방지용이다. 월 회비를 Long으로 받으면 Jackson이 1.5를 1로 잘라 받으므로,
 * BigDecimal로 받아 소수가 오면 400으로 거부한다.
 */
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
        @Digits(integer = 7, fraction = 0, message = "월 회비는 원 단위 정수로 입력하세요.")
        @DecimalMin(value = "1", message = "월 회비는 1원 이상이어야 합니다.")
        @DecimalMax(value = "1000000", message = "월 회비는 1,000,000원 이하로 입력하세요.")
        BigDecimal monthlyAmount
) {
}
