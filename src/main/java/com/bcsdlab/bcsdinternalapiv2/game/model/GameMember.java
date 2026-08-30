package com.bcsdlab.bcsdinternalapiv2.game.model;

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
 * 게임에 배정된 참여 멤버 한 명(FR-7.2, ADR-012 패턴 재사용). {@code member}는 이미 구현된
 * 명부를 직접 참조한다 — 포트 추상화 없이, 이름·등급·프로필 사진 쓰기 경로도 없다(INV-19).
 * 하위 항목이라 soft delete를 쓰지 않고 {@code game} 삭제 시 on delete cascade로 함께 지워진다.
 */
@Getter
@Entity
@Table(name = "game_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private GameMember(Game game, Member member, int displayOrder) {
        this.game = game;
        this.member = member;
        this.displayOrder = displayOrder;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
