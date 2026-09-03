package com.bcsdlab.bcsdinternalapiv2.home.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메인 화면 멘토 캐러셀에 노출되는 멤버 한 명(FR-8.1, ADR-012 패턴 재사용). 명부를
 * 직접 참조하고, 이름·사진·트랙에 대한 쓰기 경로는 만들지 않는다(INV-19). 노출
 * 여부(존재 자체)와 순서만 다룬다 — 트랙 멤버(TrackPageMember)와 달리 별도의
 * 숨김(is_visible) 토글이 없다: 슬롯에서 빼는 것 자체가 곧 숨김이다.
 */
@Getter
@Entity
@Table(name = "mentor_slot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentorSlot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private MentorSlot(Member member, int displayOrder) {
        this.member = member;
        this.displayOrder = displayOrder;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
