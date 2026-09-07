package com.bcsdlab.bcsdinternalapiv2.activity.model;

import com.bcsdlab.bcsdinternalapiv2.global.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 활동 탭(EVENT/GAME/KOIN 등). 최상위 엔티티라 soft delete를 쓴다(ADR-006).
 * 삭제 시 "활동이 남아 있으면 거부"(AC-3.8)는 Activity 엔티티가 생기는 T-15에서
 * 함께 구현한다 — 지금은 Activity가 없어 검증할 대상이 없다.
 */
@Getter
@Entity
@Table(name = "activity_category")
@SQLRestriction("deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityCategory extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false, updatable = false)
    private String slug;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "headline")
    private String headline;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Builder
    private ActivityCategory(String slug, String name, String headline, String heroImageUrl, int displayOrder,
                              boolean published) {
        this.slug = slug;
        this.name = name;
        this.headline = headline;
        this.heroImageUrl = heroImageUrl;
        this.displayOrder = displayOrder;
        this.published = published;
    }

    public void updateHeader(String name, String headline, String heroImageUrl) {
        this.name = name;
        this.headline = headline;
        this.heroImageUrl = heroImageUrl;
    }

    public void updatePublished(boolean published) {
        this.published = published;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
