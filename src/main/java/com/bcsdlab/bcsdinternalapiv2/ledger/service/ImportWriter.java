package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.ImportCommitRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ImportCommitResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerCategory;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerEntryEvidenceRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerImportRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerImportRepository.NewEntry;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer.Sequenced;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.repository.ImageAssetRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 가져오기 반영(트랜잭션 하나, 전부 반영하거나 전부 롤백). 파싱·rowKey 대조는 호출하는 쪽이 트랜잭션 밖에서 끝낸다. */
@Component
@RequiredArgsConstructor
public class ImportWriter {

    private static final int DESCRIPTION_MAX = 200;
    private static final String DEFAULT_DESCRIPTION = "가져온 거래내역";

    private final Clock clock;
    private final LedgerImportRepository importRepository;
    private final DuesSemesterRepository semesterRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final LedgerEntryEvidenceRepository evidenceRepository;
    private final EvidenceWriter evidenceWriter;

    @Transactional
    public ImportCommitResponse commit(String fileName, List<Sequenced<Row>> rows, Long adminId) {
        requireAttachableEvidences(rows.stream().flatMap(row -> row.item().source().evidenceIds().stream()).toList());
        Optional<DuesSemester> latest = semesterRepository.findAllLatestFirst().stream().findFirst();
        Instant now = Instant.now(clock);

        int created = 0;
        for (Sequenced<Row> sequenced : rows) {
            Row row = sequenced.item();
            ImportCommitRequest.Transaction source = row.source();
            LedgerCategory category = source.category().toLedgerCategory();
            Optional<Long> id = importRepository.insertIfAbsent(new NewEntry(
                    row.occurredAt(), sequenced.seq(), source.type().name(), category.name(), row.counterparty(),
                    description(source.note(), category, latest), source.note(), source.amount(), source.bankBalance(),
                    fileName, source.rowKey(), adminId));
            if (id.isPresent()) {
                created++;
                for (Long evidenceId : source.evidenceIds()) {
                    evidenceWriter.attach(evidenceId, id.get(), now);
                }
            }
        }
        return new ImportCommitResponse(created, rows.size() - created);
    }

    /** 장부 수정과 같은 규칙(설계 5-4절): 장부 증빙이어야 하고(404), 확정됐고 아직 어디에도 안 붙어 있어야 한다(400). */
    private void requireAttachableEvidences(List<Long> evidenceIds) {
        if (evidenceIds.isEmpty()) {
            return;
        }
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
        if (!evidenceRepository.findAllById(evidenceIds).isEmpty()) {
            throw new LedgerException(LedgerExceptionType.EVIDENCE_NOT_AVAILABLE);
        }
    }

    /** FE mock과 같다: 비고가 있으면 비고, 없으면 DUES는 "{최근 학기} 회비", 그 밖은 "가져온 거래내역". */
    private static String description(String note, LedgerCategory category, Optional<DuesSemester> latest) {
        String description;
        if (!note.isBlank()) {
            description = note.strip();
        } else if (category == LedgerCategory.DUES && latest.isPresent()) {
            description = "%d년 %d학기 회비".formatted(latest.get().getYear(), latest.get().getTerm());
        } else {
            description = DEFAULT_DESCRIPTION;
        }
        return description.length() > DESCRIPTION_MAX ? description.substring(0, DESCRIPTION_MAX) : description;
    }

    /** 검증을 마친 요청 거래. counterparty는 정규화한 값이다. */
    public record Row(ImportCommitRequest.Transaction source, LocalDateTime occurredAt, String counterparty) {
    }
}
