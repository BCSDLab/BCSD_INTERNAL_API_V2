package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.LedgerEntryUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntryEvidence;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesLinkRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerEntryEvidenceRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerEntryRepository;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.repository.ImageAssetRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LedgerEntryService {

    private final LedgerEntryRepository entryRepository;
    private final DuesLinkRepository linkRepository;
    private final LedgerEntryAssembler assembler;
    private final LedgerEntryEvidenceRepository evidenceRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final EvidenceWriter evidenceWriter;
    private final Clock clock;

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
        replaceEvidences(entryId, request.evidenceIds());
        if (entry.getCategory() != request.category()) {
            linkRepository.findById(entryId).ifPresent(linkRepository::delete);
        }
        entry.update(request.counterparty(), request.category(), request.description(), request.note());
        return assembler.toResponse(entry);
    }

    /**
     * 증빙 목록을 통째로 바꾼다(내역 행을 잠근 상태에서 부른다). 모두 검사한 뒤에 쓴다.
     * <ul>
     *   <li>장부 증빙이 아니거나 없으면 404, 업로드가 확정되지 않았으면 400</li>
     *   <li>다른 기록에 붙어 있으면 400. 이 기록에 이미 붙어 있으면 그대로 둔다.</li>
     *   <li>목록에서 빠진 연결은 지운다. image_asset 행과 S3 객체는 남는다(고아 정리는 범위 밖).</li>
     * </ul>
     * 개수(최대 5)와 중복은 요청 검증이 먼저 막는다.
     */
    private void replaceEvidences(Long entryId, List<Long> evidenceIds) {
        Map<Long, ImageAsset> assets = imageAssetRepository.findAllById(evidenceIds).stream()
                .filter(asset -> asset.getPurpose() == ImagePurpose.LEDGER_EVIDENCE)
                .collect(Collectors.toMap(ImageAsset::getId, Function.identity()));
        for (Long evidenceId : evidenceIds) {
            ImageAsset asset = assets.get(evidenceId);
            if (asset == null) {
                throw new LedgerException(LedgerExceptionType.EVIDENCE_NOT_FOUND);
            }
            if (!asset.isConfirmed()) {
                throw new LedgerException(LedgerExceptionType.EVIDENCE_NOT_AVAILABLE);
            }
        }
        Set<Long> alreadyHere = new HashSet<>();
        for (LedgerEntryEvidence attached : evidenceRepository.findAllById(evidenceIds)) {
            if (!attached.getEntryId().equals(entryId)) {
                throw new LedgerException(LedgerExceptionType.EVIDENCE_NOT_AVAILABLE);
            }
            alreadyHere.add(attached.getImageAssetId());
        }

        evidenceRepository.findAllByEntryId(entryId).stream()
                .filter(attached -> !evidenceIds.contains(attached.getImageAssetId()))
                .forEach(evidenceRepository::delete);
        Instant now = Instant.now(clock);
        for (Long evidenceId : evidenceIds) {
            if (!alreadyHere.contains(evidenceId)) {
                evidenceWriter.attach(evidenceId, entryId, now);
            }
        }
    }
}
