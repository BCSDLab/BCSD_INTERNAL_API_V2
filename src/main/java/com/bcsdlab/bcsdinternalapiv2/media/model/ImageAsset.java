package com.bcsdlab.bcsdinternalapiv2.media.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 미디어 라이브러리 한 항목(ADR-009). 콘텐츠 테이블과 FK로 묶지 않는다 — 이미지 행 삭제가
 * 콘텐츠를 깨뜨리지 않게 하기 위해서다. {@code is_confirmed}가 true인(=업로드가
 * {@code complete}로 끝난) 행만 라이브러리 목록에 보인다(AC-4.3, AC-4.4).
 *
 * <p>{@code image_asset} 테이블에는 {@code updated_at}이 없다 — 업로드 후 수정 없이
 * 생성/확정만 하므로 {@link com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity}를 쓰지
 * 않고 직접 매핑한다. soft delete도 쓰지 않는다(라이브러리 항목은 물리 삭제 대상이 아니다).
 */
@Getter
@Entity
@Table(name = "image_asset")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "s3_key", nullable = false, updatable = false, unique = true)
    private String s3Key;

    @Column(name = "url", nullable = false, updatable = false)
    private String url;

    @Column(name = "original_name", nullable = false, updatable = false)
    private String originalName;

    @Column(name = "content_type", nullable = false, updatable = false)
    private String contentType;

    @Column(name = "byte_size", nullable = false, updatable = false)
    private long byteSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, updatable = false)
    private ImagePurpose purpose;

    @Column(name = "is_confirmed", nullable = false)
    private boolean confirmed;

    @Column(name = "uploaded_by", updatable = false)
    private Long uploadedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    private ImageAsset(String s3Key, String url, String originalName, String contentType, long byteSize,
                        ImagePurpose purpose, Long uploadedBy) {
        this.s3Key = s3Key;
        this.url = url;
        this.originalName = originalName;
        this.contentType = contentType;
        this.byteSize = byteSize;
        this.purpose = purpose;
        this.confirmed = false;
        this.uploadedBy = uploadedBy;
    }

    public void confirm() {
        this.confirmed = true;
    }
}
