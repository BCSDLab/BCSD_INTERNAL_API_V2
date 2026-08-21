package com.bcsdlab.bcsdinternalapiv2.track.model;

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
 * 홈페이지에 노출되는 트랙 프로필. 트랙 마스터({@link TrackMaster}, "부원이 어디에 속하는가")와
 * 의도적으로 분리되어 있다 — 이 엔티티는 "홈페이지에 어떻게 보이는가"만 다룬다(ADR-002/003).
 *
 * <p>{@code @SQLRestriction}은 {@link SoftDeletableEntity}(매핑된 슈퍼클래스)가 아니라
 * 이 클래스에 직접 붙어 있다 — Hibernate가 상속시키지 않기 때문이다.
 */
@Getter
@Entity
@Table(name = "track_page")
@SQLRestriction("deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackPage extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false, updatable = false)
    private TrackMaster track;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "tagline", nullable = false)
    private String tagline;

    @Column(name = "hero_image_url")
    private String heroImageUrl;

    @Column(name = "og_image_url")
    private String ogImageUrl;

    @Column(name = "seo_description")
    private String seoDescription;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Builder
    private TrackPage(TrackMaster track, String slug, String displayName, String tagline, String heroImageUrl,
                       String ogImageUrl, String seoDescription, int displayOrder, boolean published) {
        this.track = track;
        this.slug = slug;
        this.displayName = displayName;
        this.tagline = tagline;
        this.heroImageUrl = heroImageUrl;
        this.ogImageUrl = ogImageUrl;
        this.seoDescription = seoDescription;
        this.displayOrder = displayOrder;
        this.published = published;
    }

    public void updateHeader(String displayName, String tagline, String heroImageUrl, String ogImageUrl,
                              String seoDescription) {
        this.displayName = displayName;
        this.tagline = tagline;
        this.heroImageUrl = heroImageUrl;
        this.ogImageUrl = ogImageUrl;
        this.seoDescription = seoDescription;
    }

    public void changeSlug(String slug) {
        this.slug = slug;
    }

    public void updatePublished(boolean published) {
        this.published = published;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
