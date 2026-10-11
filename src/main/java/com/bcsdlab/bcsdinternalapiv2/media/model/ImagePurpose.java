package com.bcsdlab.bcsdinternalapiv2.media.model;

/**
 * {@link #LEDGER_EVIDENCE}는 장부 증빙 전용이다. 장부 API(검증이 다른 전용 경로)로만 만들고 확정하며,
 * 미디어 라이브러리 API(발급·확정·목록)는 이 값을 거부한다.
 */
public enum ImagePurpose {
    TRACK_HERO,
    STUDY_ICON,
    TECH_ICON,
    ACTIVITY,
    ACTIVITY_CONTENT,
    GAME,
    GAME_CONTENT,
    ETC,
    LEDGER_EVIDENCE;

    /** 미디어 라이브러리 API로 다룰 수 없는 목적. */
    public boolean isLibraryRestricted() {
        return this == LEDGER_EVIDENCE;
    }
}
