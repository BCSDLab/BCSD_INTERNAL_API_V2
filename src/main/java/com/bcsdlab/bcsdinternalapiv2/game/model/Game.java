package com.bcsdlab.bcsdinternalapiv2.game.model;

import com.bcsdlab.bcsdinternalapiv2.global.SoftDeletableEntity;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
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
 * 게임 정보(FR-7). 트랙 페이지({@link TrackMaster})와 마찬가지로 최상위 엔티티라 soft delete를
 * 쓴다. 이번 1차 범위는 메타데이터만이며, 게임 빌드 파일 업로드·서빙은 ADR-023에 따라
 * 후속 태스크(T-41)에서 다룬다.
 *
 * <p>{@code @SQLRestriction}은 {@link SoftDeletableEntity}(매핑된 슈퍼클래스)가 아니라
 * 이 클래스에 직접 붙여야 한다 — Hibernate가 상속시키지 않기 때문이다.
 */
@Getter
@Entity
@Table(name = "game")
@SQLRestriction("deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "one_liner", nullable = false)
    private String oneLiner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private TrackMaster track;

    @Column(name = "team_label")
    private String teamLabel;

    @Column(name = "description")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Builder
    private Game(String slug, String name, String oneLiner, TrackMaster track, String teamLabel,
                 String description, String thumbnailUrl, int displayOrder, boolean published) {
        this.slug = slug;
        this.name = name;
        this.oneLiner = oneLiner;
        this.track = track;
        this.teamLabel = teamLabel;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.displayOrder = displayOrder;
        this.published = published;
    }

    public void updatePublished(boolean published) {
        this.published = published;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
