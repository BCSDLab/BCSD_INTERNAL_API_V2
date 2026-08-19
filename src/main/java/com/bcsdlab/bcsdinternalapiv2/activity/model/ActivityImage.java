package com.bcsdlab.bcsdinternalapiv2.activity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 활동 사진. 하위 트리라 soft delete 없이 on delete cascade로 물리 삭제하고, 전체
 * 교체(T-15)로만 관리한다. display_order=0이 목록 썸네일이다(INV-12).
 *
 * <p>{@code activity_image} 테이블에는 {@code updated_at}이 없다 — 사진은 개별 수정 없이
 * 항상 전체 교체(삭제 후 재생성)로만 다루므로 생성 시각만 있으면 된다. 그래서
 * {@link com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity}를 쓰지 않고 직접 매핑한다.
 */
@Getter
@Entity
@Table(name = "activity_image")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false, updatable = false)
    private Activity activity;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    private ActivityImage(Activity activity, String imageUrl, int displayOrder) {
        this.activity = activity;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }
}
