package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import static com.bcsdlab.bcsdinternalapiv2.ledger.repository.ConstraintViolations.isViolationOf;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntryEvidence;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerEntryEvidenceRepository;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 증빙 DB 쓰기. S3 확인(네트워크)은 호출하는 쪽이 트랜잭션 밖에서 끝낸 뒤 이 빈을 부른다. */
@Component
@RequiredArgsConstructor
public class EvidenceWriter {

    private static final String EVIDENCE_PRIMARY_KEY = "ledger_entry_evidence_pkey";

    private final EvidenceReader evidenceReader;
    private final LedgerEntryEvidenceRepository evidenceRepository;

    /** 업로드 완료 확정. 이미 확정됐으면 그대로다. */
    @Transactional
    public ImageAsset confirm(Long evidenceId) {
        ImageAsset asset = evidenceReader.get(evidenceId);
        asset.confirm();
        return asset;
    }

    /**
     * 증빙을 장부 기록에 붙인다. 호출하는 쪽이 사전 확인을 하지만, 그 사이 다른 요청이 같은 증빙을 다른 기록에
     * 붙이면 PK 위반이 난다. 설계(5-4절)대로 400 EVIDENCE_NOT_AVAILABLE로 바꾼다.
     */
    @Transactional
    public void attach(Long evidenceId, Long entryId, Instant attachedAt) {
        try {
            evidenceRepository.saveAndFlush(new LedgerEntryEvidence(evidenceId, entryId, attachedAt));
        } catch (DataIntegrityViolationException e) {
            if (isViolationOf(e, EVIDENCE_PRIMARY_KEY)) {
                throw new LedgerException(LedgerExceptionType.EVIDENCE_NOT_AVAILABLE);
            }
            throw e;
        }
    }
}
