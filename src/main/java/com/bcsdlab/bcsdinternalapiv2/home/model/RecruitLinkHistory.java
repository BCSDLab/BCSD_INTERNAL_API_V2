package com.bcsdlab.bcsdinternalapiv2.home.model;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** 모집 링크 변경 이력. insert-only — 수정·삭제 경로가 없다. */
@Getter
@Entity
@Table(name = "recruit_link_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitLinkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_form_url", nullable = false)
    private String googleFormUrl;

    @Column(name = "is_open", nullable = false)
    private boolean open;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Member changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Builder
    private RecruitLinkHistory(String googleFormUrl, boolean open, Member changedBy, Instant changedAt) {
        this.googleFormUrl = googleFormUrl;
        this.open = open;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }
}
