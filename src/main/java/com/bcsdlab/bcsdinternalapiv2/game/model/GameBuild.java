package com.bcsdlab.bcsdinternalapiv2.game.model;

import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 게임 빌드 메타데이터(FR-7.6). 이번 1차에서는 버전·상태만 다루고 실제 ZIP 파일은
 * 저장·서빙하지 않는다 — {@code buildFileUrl}은 항상 null일 수 있다(ADR-023 §범위).
 * 업로드·압축해제·서빙 파이프라인은 후속 태스크(T-41)에서 구현한다.
 */
@Getter
@Entity
@Table(name = "game_build")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameBuild extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    private Game game;

    @Column(name = "version", nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameBuildStatus status;

    @Column(name = "canvas_width")
    private Integer canvasWidth;

    @Column(name = "canvas_height")
    private Integer canvasHeight;

    @Column(name = "storage_bytes")
    private Long storageBytes;

    @Column(name = "build_file_url")
    private String buildFileUrl;

    @Column(name = "failure_reason")
    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private Member uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Builder
    private GameBuild(Game game, String version, GameBuildStatus status, Integer canvasWidth,
                       Integer canvasHeight, Long storageBytes, String buildFileUrl, Member uploadedBy,
                       Instant uploadedAt) {
        this.game = game;
        this.version = version;
        this.status = status;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.storageBytes = storageBytes;
        this.buildFileUrl = buildFileUrl;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public void updateStatus(GameBuildStatus status) {
        this.status = status;
    }

    /**
     * 완료 웹훅(ACTIVE)을 반영한다. 업로드 중(PROCESSING)인 빌드만 활성화한다 — 늦게 도착하거나 다시 보낸 웹훅이
     * 보관된 옛 빌드를 되살리지 못하게 한다. 이미 ACTIVE면 재전송으로 보고 아무것도 바꾸지 않는다.
     *
     * @return 이번 호출로 활성화됐으면 true
     */
    public boolean applyActive(Integer canvasWidth, Integer canvasHeight, Long storageBytes, String buildFileUrl) {
        if (status == GameBuildStatus.ACTIVE) {
            return false;
        }
        requireProcessing();
        this.status = GameBuildStatus.ACTIVE;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.storageBytes = storageBytes;
        this.buildFileUrl = buildFileUrl;
        this.failureReason = null;
        return true;
    }

    /** 실패 웹훅(FAILED)을 반영한다. 업로드 중인 빌드만 실패 처리하고, 이미 FAILED면 아무것도 바꾸지 않는다. */
    public void markFailed(String failureReason) {
        if (status == GameBuildStatus.FAILED) {
            return;
        }
        requireProcessing();
        this.status = GameBuildStatus.FAILED;
        this.failureReason = failureReason;
    }

    private void requireProcessing() {
        if (status != GameBuildStatus.PROCESSING) {
            throw new GameException(GameExceptionType.GAME_BUILD_INVALID_STATE);
        }
    }
}
