package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerCategory;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LinkStatus;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * occurredAt은 오프셋 없는 KST 벽시계 시각("2026-11-16T09:10:00")이다. 초가 0이어도 생략하지 않도록 직접 만든다.
 * balance는 은행 거래 후 잔액, source는 가져온 파일명이다.
 */
public record LedgerEntryResponse(
        Long id,
        String occurredAt,
        EntryType type,
        LedgerCategory category,
        String counterparty,
        String description,
        String note,
        long amount,
        long balance,
        String source,
        LinkStatus linkStatus,
        DuesLinkResponse duesLink,
        List<EvidenceResponse> evidences
) {

    private static final DateTimeFormatter OCCURRED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static LedgerEntryResponse of(LedgerEntry entry, DuesLinkResponse duesLink,
                                         List<EvidenceResponse> evidences) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getOccurredAt().format(OCCURRED_AT_FORMAT),
                entry.getType(),
                entry.getCategory(),
                entry.getCounterparty(),
                entry.getDescription(),
                entry.getNote(),
                entry.getAmount(),
                entry.getBankBalance(),
                entry.getSourceFileName(),
                LinkStatus.of(entry.getCategory(), duesLink != null),
                duesLink,
                evidences);
    }
}
