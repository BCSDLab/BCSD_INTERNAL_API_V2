package com.bcsdlab.bcsdinternalapiv2.curriculum.model;

import com.bcsdlab.bcsdinternalapiv2.global.SoftDeletableEntity;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
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
 * 트랙 페이지의 커리큘럼 세트("비기너", "심화"). 트랙당 공개 세트는 하나뿐이다
 * (INV-2, {@code uq_curriculum_published} partial unique index).
 */
@Getter
@Entity
@Table(name = "curriculum")
@SQLRestriction("deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_page_id", nullable = false, updatable = false)
    private TrackPage trackPage;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private Curriculum(TrackPage trackPage, String name, boolean published, int displayOrder) {
        this.trackPage = trackPage;
        this.name = name;
        this.published = published;
        this.displayOrder = displayOrder;
    }

    public void rename(String name) {
        this.name = name;
    }

    public void updatePublished(boolean published) {
        this.published = published;
    }
}
