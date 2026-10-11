package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;

/**
 * 장부 수정. 전체를 교체한다. 금액·시각·입출금 구분은 은행 거래 원본이라 바꿀 수 없다.
 * 상대·내용은 앞뒤 공백을 지운 뒤 검사한다.
 */
public record LedgerEntryUpdateRequest(
        @NotBlank(message = "거래 상대를 입력하세요.")
        @Size(max = 100, message = "거래 상대는 100자 이하로 입력하세요.")
        String counterparty,

        @NotNull(message = "분류를 선택하세요.")
        LedgerCategory category,

        @NotBlank(message = "내용을 입력하세요.")
        @Size(max = 200, message = "내용은 200자 이하로 입력하세요.")
        String description,

        @NotNull(message = "비고를 입력하세요.")
        @Size(max = 500, message = "비고는 500자 이하로 입력하세요.")
        String note,

        @NotNull(message = "증빙 목록을 입력하세요.")
        @Size(max = 5, message = "증빙은 최대 5개까지 첨부할 수 있습니다.")
        List<@NotNull Long> evidenceIds
) {

    public LedgerEntryUpdateRequest {
        counterparty = counterparty == null ? null : counterparty.strip();
        description = description == null ? null : description.strip();
    }

    @Schema(hidden = true)
    @AssertTrue(message = "같은 증빙이 요청에 두 번 있습니다.")
    public boolean isEvidenceIdsDistinct() {
        return evidenceIds == null || new HashSet<>(evidenceIds).size() == evidenceIds.size();
    }
}
