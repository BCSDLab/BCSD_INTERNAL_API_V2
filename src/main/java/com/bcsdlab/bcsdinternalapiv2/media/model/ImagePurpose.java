package com.bcsdlab.bcsdinternalapiv2.media.model;

/**
 * {@link #LEDGER_EVIDENCE}는 예전 장부 증빙(S3 업로드) 행에만 남아 있다. 증빙이 구글 드라이브 링크로 바뀌어(V27)
 * 새로 만들지 않으며, 기존 행을 보호하려고 미디어 라이브러리 API(발급·확정·목록)는 이 값을 계속 거부한다.
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
