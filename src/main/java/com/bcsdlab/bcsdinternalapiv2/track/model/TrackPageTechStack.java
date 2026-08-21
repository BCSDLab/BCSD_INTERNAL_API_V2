package com.bcsdlab.bcsdinternalapiv2.track.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 트랙 페이지 ↔ 기술스택 다대다 조인 + 표시 순서. 전체 교체(T-09)로만 관리한다.
 */
@Getter
@Entity
@Table(name = "track_page_tech_stack")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackPageTechStack {

    @EmbeddedId
    private TrackPageTechStackId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("trackPageId")
    @JoinColumn(name = "track_page_id")
    private TrackPage trackPage;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("techStackId")
    @JoinColumn(name = "tech_stack_id")
    private TechStack techStack;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public TrackPageTechStack(TrackPage trackPage, TechStack techStack, int displayOrder) {
        this.id = new TrackPageTechStackId(trackPage.getId(), techStack.getId());
        this.trackPage = trackPage;
        this.techStack = techStack;
        this.displayOrder = displayOrder;
    }
}
