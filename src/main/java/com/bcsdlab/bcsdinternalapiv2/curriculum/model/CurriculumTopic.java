package com.bcsdlab.bcsdinternalapiv2.curriculum.model;

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
 * 주차의 토픽. 랜딩 번호(1,2,3…)는 별도 컬럼이 아니라 display_order + 1로 렌더한다(ADR-005) —
 * 순서를 바꾸면 번호가 자동으로 재부여된다(AC-2.6).
 */
@Getter
@Entity
@Table(name = "curriculum_topic")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurriculumTopic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false, updatable = false)
    private CurriculumWeek week;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private CurriculumTopic(CurriculumWeek week, String title, int displayOrder) {
        this.week = week;
        this.title = title;
        this.displayOrder = displayOrder;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
