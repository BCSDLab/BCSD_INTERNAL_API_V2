package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 면제 저장 전 미리보기. 검증은 저장과 같다. editingId가 있으면 그 면제를 이 값으로 바꾼 경우다. */
public record ExemptionPreviewRequest(
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
        String endMonth,

        Long editingId
) {

    public ExemptionPreviewRequest {
        reason = ExemptionDraft.normalizeReason(reason);
    }

    public ExemptionDraft toDraft() {
        return new ExemptionDraft(memberId, reason, note, ExemptionDraft.parseMonth(startMonth),
                ExemptionDraft.parseMonth(endMonth));
    }
}
