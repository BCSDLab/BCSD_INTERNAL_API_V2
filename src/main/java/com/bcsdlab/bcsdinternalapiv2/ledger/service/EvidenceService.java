package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.config.LedgerEvidenceProperties;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.EvidencePresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.EvidencePresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.EvidenceResponse;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.PresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.service.AdminImageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 장부 증빙 업로드: presigned URL 발급 → 브라우저가 S3에 직접 PUT → complete.
 *
 * <p>발급 때 Content-Type과 Content-Length를 presign에 서명해, S3가 다른 형식·크기의 업로드를 거부하게 한다.
 * complete는 설정(app.ledger.evidence.verify-upload)이 켜져 있으면 S3 HeadObject로 실제 객체를 한 번 더 확인한다.
 * S3 호출은 트랜잭션 밖에서 하고, DB 쓰기는 {@link EvidenceWriter}가 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceService {

    private final AdminImageService adminImageService;
    private final EvidenceReader evidenceReader;
    private final EvidenceWriter evidenceWriter;
    private final EvidenceUploadVerifier uploadVerifier;
    private final LedgerEvidenceProperties properties;

    @PostConstruct
    void warnIfVerificationDisabled() {
        if (!properties.verifyUpload()) {
            log.warn("LEDGER_EVIDENCE_VERIFY_UPLOAD=false — 증빙 완료 때 S3 HeadObject 확인을 하지 않는다. "
                    + "발급 단계 검증(확장자·형식·크기)과 presign 서명만 남는다.");
        }
    }

    public EvidencePresignedUrlResponse issue(EvidencePresignedUrlRequest request, Long adminId) {
        PresignedUrlResponse issued = adminImageService.issueUpload(ImagePurpose.LEDGER_EVIDENCE, request.fileName(),
                request.contentType(), request.size(), adminId, true);
        return new EvidencePresignedUrlResponse(issued.imageId(), issued.uploadUrl());
    }

    /** 이미 확정된 증빙이면 다시 확인하지 않고 그대로 돌려준다. */
    public EvidenceResponse complete(Long evidenceId) {
        ImageAsset asset = evidenceReader.get(evidenceId);
        if (asset.isConfirmed()) {
            return EvidenceResponse.from(asset);
        }
        if (properties.verifyUpload()) {
            uploadVerifier.verify(asset);
        }
        return EvidenceResponse.from(evidenceWriter.confirm(evidenceId));
    }
}
