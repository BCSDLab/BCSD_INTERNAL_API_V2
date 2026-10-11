package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.EvidenceFormat;
import com.bcsdlab.bcsdinternalapiv2.global.config.S3Properties;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * S3 HeadObject로 업로드된 객체가 신청한 크기·형식과 같은지 확인한다. 네트워크 호출이라 트랜잭션 밖에서 부른다.
 *
 * <p>HEAD 응답에는 본문이 없어 오류 코드(AccessDenied 등)가 비어 오므로 상태 코드로 판단한다. S3는 호출자에게
 * s3:ListBucket 권한이 없으면 없는 객체에도 404 대신 403을 준다. 그래서 403은 "권한 부족 또는 객체 없음"이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EvidenceUploadVerifier {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public void verify(ImageAsset asset) {
        HeadObjectResponse head = headObject(asset);
        Long actualSize = head.contentLength();
        String actualType = head.contentType() == null ? null : EvidenceFormat.baseType(head.contentType());
        boolean sizeMatches = actualSize != null && actualSize == asset.getByteSize()
                && actualSize <= EvidenceFormat.MAX_BYTES;
        boolean typeMatches = EvidenceFormat.baseType(asset.getContentType()).equals(actualType);
        if (!sizeMatches || !typeMatches) {
            log.warn("LEDGER_EVIDENCE_UPLOAD_MISMATCH id={} expected=({}, {}) actual=({}, {})",
                    asset.getId(), asset.getByteSize(), asset.getContentType(), actualSize, head.contentType());
            throw new LedgerException(LedgerExceptionType.EVIDENCE_NOT_UPLOADED);
        }
    }

    private HeadObjectResponse headObject(ImageAsset asset) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(asset.getS3Key())
                    .build());
        } catch (NoSuchKeyException e) {
            throw new LedgerException(LedgerExceptionType.EVIDENCE_NOT_UPLOADED);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new LedgerException(LedgerExceptionType.EVIDENCE_NOT_UPLOADED);
            }
            if (e.statusCode() == 403) {
                log.error("LEDGER_EVIDENCE_VERIFY_FORBIDDEN id={} key={} — 권한 부족: 서버 IAM에 s3:GetObject"
                                + "(HeadObject)와 s3:ListBucket 권한을 추가하거나, LEDGER_EVIDENCE_VERIFY_UPLOAD=false로 "
                                + "업로드 확인을 끄세요.", asset.getId(), asset.getS3Key(), e);
                throw new LedgerException(LedgerExceptionType.EVIDENCE_VERIFY_FORBIDDEN);
            }
            log.error("LEDGER_EVIDENCE_VERIFY_FAILED id={} status={}", asset.getId(), e.statusCode(), e);
            throw new LedgerException(LedgerExceptionType.EVIDENCE_VERIFY_FAILED);
        } catch (SdkException e) {
            log.error("LEDGER_EVIDENCE_VERIFY_FAILED id={}", asset.getId(), e);
            throw new LedgerException(LedgerExceptionType.EVIDENCE_VERIFY_FAILED);
        }
    }
}
