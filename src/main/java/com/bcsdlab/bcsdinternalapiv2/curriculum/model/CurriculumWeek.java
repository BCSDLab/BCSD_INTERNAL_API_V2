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
 * 커리큘럼의 주차. 단일 숫자(weekTo=null) 또는 범위(예: 14~17주차)로 표현한다.
 * 하위 트리라 soft delete를 쓰지 않고 on delete cascade로 물리 삭제한다(INV-7).
 */
@Getter
@Entity
@Table(name = "curriculum_week")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurriculumWeek extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false, updatable = false)
    private Curriculum curriculum;

    @Column(name = "week_from", nullable = false)
    private int weekFrom;

    @Column(name = "week_to")
    private Integer weekTo;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private CurriculumWeek(Curriculum curriculum, int weekFrom, Integer weekTo, int displayOrder) {
        this.curriculum = curriculum;
        this.weekFrom = weekFrom;
        this.weekTo = weekTo;
        this.displayOrder = displayOrder;
    }

    public void updateLabel(int weekFrom, Integer weekTo) {
        this.weekFrom = weekFrom;
        this.weekTo = weekTo;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
