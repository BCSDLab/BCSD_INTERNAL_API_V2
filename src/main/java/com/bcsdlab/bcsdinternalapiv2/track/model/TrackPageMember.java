package com.bcsdlab.bcsdinternalapiv2.track.model;

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
 * 트랙 페이지에 배정된 부원 한 명(ADR-012). {@code member}는 이미 구현된 명부를 그대로
 * 참조한다 — 포트 추상화 없이 {@code MemberRepository}를 직접 조회한다. 이름·등급·프로필
 * 사진에 대한 쓰기 경로는 만들지 않는다(INV-13). 하위 항목이라 soft delete를 쓰지 않고
 * {@code track_page} 삭제 시 DB의 on delete cascade로 함께 지워진다.
 */
@Getter
@Entity
@Table(name = "track_page_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackPageMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_page_id", nullable = false, updatable = false)
    private TrackPage trackPage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Builder
    private TrackPageMember(TrackPage trackPage, Member member, int displayOrder, boolean visible) {
        this.trackPage = trackPage;
        this.member = member;
        this.displayOrder = displayOrder;
        this.visible = visible;
    }

    public void updateVisible(boolean visible) {
        this.visible = visible;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
