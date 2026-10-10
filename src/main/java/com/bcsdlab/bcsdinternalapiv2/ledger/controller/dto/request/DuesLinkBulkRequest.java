package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** 입출금 내역 연결 모달의 일괄 저장. 1~1,000건. */
public record DuesLinkBulkRequest(
        @NotEmpty(message = "연결할 내역을 선택하세요.")
        @Size(max = 1000, message = "한 번에 1,000건까지 연결할 수 있습니다.")
        List<@Valid @NotNull Link> links
) {

    public record Link(
            @NotNull(message = "장부 기록을 선택하세요.")
            Long entryId,

            @NotNull(message = "회원을 선택하세요.")
            Long memberId
    ) {
    }
}
