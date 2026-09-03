package com.bcsdlab.bcsdinternalapiv2.game.model;

/** 게임 빌드 상태(FR-7.6). 이번 1차는 메타데이터만 다루므로 ACTIVE도 실제 파일을 보장하지 않는다. */
public enum GameBuildStatus {
    PENDING,
    PROCESSING,
    ACTIVE,
    ARCHIVED,
    FAILED,
}
