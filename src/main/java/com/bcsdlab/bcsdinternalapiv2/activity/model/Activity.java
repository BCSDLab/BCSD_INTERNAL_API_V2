package com.bcsdlab.bcsdinternalapiv2.activity.model;

import com.bcsdlab.bcsdinternalapiv2.global.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 활동 타임라인의 한 항목. 최상위 엔티티라 soft delete를 쓴다(ADR-006). year/month는
 * date가 아니라 정수 두 개다 — 일(day) 개념이 없고 랜딩의 연도 그룹핑에 그대로 맞는다(ADR-007).
 */
@Getter
@Entity
@Table(name = "activity")
@SQLRestriction("deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private ActivityCategory category;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Builder
    private Activity(ActivityCategory category, int year, int month, String title, String summary, String content,
                      String externalUrl, int displayOrder, boolean published) {
        this.category = category;
        this.year = year;
        this.month = month;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.externalUrl = externalUrl;
        this.displayOrder = displayOrder;
        this.published = published;
    }

    public void updateContent(int year, int month, String title, String summary, String content,
                               String externalUrl) {
        this.year = year;
        this.month = month;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.externalUrl = externalUrl;
    }

    public void updatePublished(boolean published) {
        this.published = published;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean hasDetail() {
        return content != null && !content.isBlank();
    }
}
