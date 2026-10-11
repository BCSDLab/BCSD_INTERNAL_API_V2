package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.LedgerEntryUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesLinkRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LedgerEntryService {

    private final LedgerEntryRepository entryRepository;
    private final DuesLinkRepository linkRepository;
    private final LedgerEntryAssembler assembler;
    private final EvidenceLinkWriter evidenceLinkWriter;

    /** 쿼리 파라미터(필터)는 받지 않는다. FE가 전체를 받아 클라이언트에서 거른다. */
    @Transactional(readOnly = true)
    public LedgerEntryListResponse getEntries() {
        return new LedgerEntryListResponse(assembler.toResponses(entryRepository.findAllLatestFirst()));
    }

    /**
     * 분류·상대·내용·비고를 통째로 바꾼다. 분류가 바뀌면 회비 연결을 끊는다(새 분류가 DUES면 PENDING,
     * 그 밖이면 NONE). 분류가 같으면 연결을 유지한다. 연결 작업과 엇갈리지 않게 내역 행을 FOR UPDATE로 잠근다.
     */
    @Transactional
    public LedgerEntryResponse update(Long entryId, LedgerEntryUpdateRequest request) {
        LedgerEntry entry = entryRepository.findByIdForUpdate(entryId)
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.ENTRY_NOT_FOUND));
        evidenceLinkWriter.replace(entryId, request.evidenceLinks());
        if (entry.getCategory() != request.category()) {
            linkRepository.findById(entryId).ifPresent(linkRepository::delete);
        }
        entry.update(request.counterparty(), request.category(), request.description(), request.note());
        return assembler.toResponse(entry);
    }
}
