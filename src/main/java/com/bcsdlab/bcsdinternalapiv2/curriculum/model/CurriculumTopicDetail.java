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
 * 토픽의 세부 항목. 개별 CRUD가 아니라 전체 교체(T-11)로만 관리한다 — 에디터가
 * "Enter로 연속 입력"하는 텍스트 목록이라 문자열 배열 하나를 그대로 반영하는 편이 맞다.
 */
@Getter
@Entity
@Table(name = "curriculum_topic_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurriculumTopicDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false, updatable = false)
    private CurriculumTopic topic;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private CurriculumTopicDetail(CurriculumTopic topic, String content, int displayOrder) {
        this.topic = topic;
        this.content = content;
        this.displayOrder = displayOrder;
    }
}
