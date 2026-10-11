package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.ImportCategory;
import java.util.List;

/** 파싱만 하고 저장하지 않는다. 이미 장부에 있는 거래는 빠진다. 거래는 장부 순서(시각, 같은 초 순서)다. */
public record ImportPreviewResponse(
        String fileName,
        List<Transaction> transactions
) {

    /**
     * @param occurredAt  "2026-11-16T09:10:00"(KST, 오프셋 없음)
     * @param bankBalance 은행 거래 후 잔액. 반영하면 장부 balance가 된다
     */
    public record Transaction(
            String rowKey,
            String occurredAt,
            EntryType type,
            String counterparty,
            long amount,
            long bankBalance,
            ImportCategory suggestedCategory,
            Long suggestedMemberId
    ) {
    }
}
