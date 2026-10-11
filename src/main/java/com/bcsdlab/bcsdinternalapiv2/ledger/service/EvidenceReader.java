package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.repository.ImageAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 장부 증빙(image_asset 중 purpose=LEDGER_EVIDENCE)만 찾는다. 일반 미디어 ID는 없는 것으로 본다(404). */
@Component
@RequiredArgsConstructor
public class EvidenceReader {

    private final ImageAssetRepository imageAssetRepository;

    @Transactional(readOnly = true)
    public ImageAsset get(Long evidenceId) {
        return imageAssetRepository.findById(evidenceId)
                .filter(asset -> asset.getPurpose() == ImagePurpose.LEDGER_EVIDENCE)
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.EVIDENCE_NOT_FOUND));
    }
}
