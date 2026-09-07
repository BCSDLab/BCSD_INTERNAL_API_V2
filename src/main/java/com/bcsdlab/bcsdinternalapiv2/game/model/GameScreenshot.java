package com.bcsdlab.bcsdinternalapiv2.game.model;

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
 * 게임 스크린샷 한 장. 개별 CRUD가 아니라 전체 교체(T-38)로만 관리한다(FR-7.4).
 * 하위 트리라 soft delete를 쓰지 않고 {@code on delete cascade}로 물리 삭제한다.
 */
@Getter
@Entity
@Table(name = "game_screenshot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameScreenshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    private Game game;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private GameScreenshot(Game game, String imageUrl, int displayOrder) {
        this.game = game;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }
}
