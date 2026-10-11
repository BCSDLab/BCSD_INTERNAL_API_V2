package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.ImportCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 미리보기의 모든 거래를 반영한다(1~5,000건). 서버는 미리보기 결과를 들고 있지 않으므로 거래 원본 값을 함께 받고,
 * rowKey를 다시 계산해 다르면 400이다.
 */
public record ImportCommitRequest(
        @NotBlank(message = "파일 이름이 없습니다.")
        @Size(max = 255, message = "파일 이름은 255자 이하여야 합니다.")
        String fileName,

        @NotEmpty(message = "반영할 거래가 없습니다.")
        @Size(max = 5000, message = "한 번에 5,000건까지 반영할 수 있습니다.")
        List<@Valid @NotNull Transaction> transactions
) {

    public record Transaction(
            @NotNull(message = "rowKey가 없습니다.")
            @Pattern(regexp = "^[0-9a-f]{64}$", message = "rowKey 형식이 올바르지 않습니다.")
            String rowKey,

            @NotNull(message = "거래일시가 없습니다.")
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "거래일시 형식이 올바르지 않습니다.")
            String occurredAt,

            @NotNull(message = "입출금 구분이 없습니다.")
            EntryType type,

            @NotNull(message = "거래 상대가 없습니다.")
            @Size(max = 100, message = "거래 상대는 100자 이하여야 합니다.")
            String counterparty,

            @NotNull(message = "금액이 없습니다.")
            @Min(value = 1, message = "금액은 1원 이상이어야 합니다.")
            Long amount,

            @NotNull(message = "은행 잔액(bankBalance)이 없습니다.")
            Long bankBalance,

            @NotNull(message = "분류를 선택하세요.")
            ImportCategory category,

            @NotNull(message = "비고를 입력하세요.")
            @Size(max = 500, message = "비고는 500자 이하로 입력하세요.")
            String note,

            @NotNull(message = "증빙 목록을 입력하세요.")
            @Size(max = 5, message = "증빙은 최대 5개까지 첨부할 수 있습니다.")
            List<@Valid @NotNull(message = "증빙 링크를 입력하세요.") EvidenceLinkRequest> evidenceLinks
    ) {
    }
}
