package com.bcsdlab.bcsdinternalapiv2.track.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
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

/**
 * 트랙 페이지의 "WHAT WE STUDY" 카드. 개별 CRUD가 아니라 전체 교체(T-08)로만 관리한다 —
 * 항목이 최대 4개이고 화면이 "추가·삭제·드래그 후 저장" 한 덩어리이기 때문이다.
 * 하위 트리라 soft delete를 쓰지 않고 {@code on delete cascade}로 물리 삭제한다.
 */
@Getter
@Entity
@Table(name = "track_study_point")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackStudyPoint extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_page_id", nullable = false, updatable = false)
    private TrackPage trackPage;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "icon_image_url")
    private String iconImageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private TrackStudyPoint(TrackPage trackPage, String title, String description, String iconImageUrl,
                             int displayOrder) {
        this.trackPage = trackPage;
        this.title = title;
        this.description = description;
        this.iconImageUrl = iconImageUrl;
        this.displayOrder = displayOrder;
    }
}
