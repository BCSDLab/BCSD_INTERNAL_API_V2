package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 면제 추가·수정 공통 본문. 사유는 앞뒤 공백 제거와 NFC 정규화를 한 뒤 검사한다.
 * 종료 월이 시작 월보다 빠르면 서비스가 400 INVALID_EXEMPTION_PERIOD로 거부한다.
 */
public record ExemptionUpsertRequest(
        @NotNull(message = "회원을 선택하세요.")
        Long memberId,

        @NotBlank(message = "면제 사유를 입력하세요.")
        @Size(max = 50, message = "면제 사유는 50자 이하로 입력하세요.")
        String reason,

        @NotNull(message = "비고를 입력하세요.")
        @Size(max = 500, message = "비고는 500자 이하로 입력하세요.")
        String note,

        @NotNull(message = "시작 월을 입력하세요.")
        @Pattern(regexp = ExemptionDraft.MONTH_PATTERN, message = "월은 YYYY-MM 형식으로 입력하세요.")
        String startMonth,

        @Pattern(regexp = ExemptionDraft.MONTH_PATTERN, message = "월은 YYYY-MM 형식으로 입력하세요.")
        String endMonth
) {

    public ExemptionUpsertRequest {
        reason = ExemptionDraft.normalizeReason(reason);
    }

    public ExemptionDraft toDraft() {
        return new ExemptionDraft(memberId, reason, note, ExemptionDraft.parseMonth(startMonth),
                ExemptionDraft.parseMonth(endMonth));
    }
}
