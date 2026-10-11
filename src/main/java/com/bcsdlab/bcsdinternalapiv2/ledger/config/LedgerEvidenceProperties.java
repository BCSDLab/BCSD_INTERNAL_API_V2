package com.bcsdlab.bcsdinternalapiv2.ledger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 장부 증빙 설정. verifyUpload가 true면 업로드 완료 때 S3 HeadObject로 실제 객체의 크기·형식을 확인한다.
 * 운영 IAM에 HeadObject 권한(s3:GetObject)이 없을 때만 LEDGER_EVIDENCE_VERIFY_UPLOAD=false로 끈다.
 * 값이 빠져도 확인하는 쪽(true)으로 둔다.
 */
@ConfigurationProperties(prefix = "app.ledger.evidence")
public record LedgerEvidenceProperties(@DefaultValue("true") boolean verifyUpload) {
}
