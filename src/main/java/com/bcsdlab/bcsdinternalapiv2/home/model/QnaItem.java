package com.bcsdlab.bcsdinternalapiv2.home.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
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

/** 메인 화면 하단 Q&A 항목(FR-8.2). 텍스트만 지원한다(링크·첨부 없음). */
@Getter
@Entity
@Table(name = "qna_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QnaItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "answer", nullable = false)
    private String answer;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Builder
    private QnaItem(String question, String answer, int displayOrder, boolean published) {
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
        this.published = published;
    }

    public void update(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public void updatePublished(boolean published) {
        this.published = published;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
