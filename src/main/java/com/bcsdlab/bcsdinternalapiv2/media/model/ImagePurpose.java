package com.bcsdlab.bcsdinternalapiv2.media.model;

/**
 * {@link #LEDGER_EVIDENCE}는 예전 장부 증빙(S3 업로드) 행에만 남아 있다. 증빙이 구글 드라이브 링크로 바뀌어(V27)
 * 새로 만들지 않으며, 기존 행을 보호하려고 미디어 라이브러리 API(발급·확정·목록)는 이 값을 계속 거부한다.
 *
 * <p>{@link UploadAccess}는 그 이미지를 쓰는 화면의 권한과 맞춘다. 게임·트랙 페이지는 관리자가 아니어도 고칠 수
 * 있으므로(#125) 그 화면의 이미지 업로드도 같은 범위로 연다.
 */
public enum ImagePurpose {
    TRACK_HERO(UploadAccess.OWN_TRACK),
    STUDY_ICON(UploadAccess.OWN_TRACK),
    TECH_ICON(UploadAccess.ADMIN),
    ACTIVITY(UploadAccess.ADMIN),
    ACTIVITY_CONTENT(UploadAccess.ADMIN),
    GAME(UploadAccess.GAME_TRACK),
    GAME_CONTENT(UploadAccess.GAME_TRACK),
    ETC(UploadAccess.ADMIN),
    LEDGER_EVIDENCE(UploadAccess.ADMIN);

    private final UploadAccess uploadAccess;

    ImagePurpose(UploadAccess uploadAccess) {
        this.uploadAccess = uploadAccess;
    }

    /** 미디어 라이브러리 API로 다룰 수 없는 목적. */
    public boolean isLibraryRestricted() {
        return this == LEDGER_EVIDENCE;
    }

    public UploadAccess uploadAccess() {
        return uploadAccess;
    }

    public enum UploadAccess {
        /** 관리자만. */
        ADMIN,
        /** 관리자, 또는 GAME 트랙의 멘토·정회원. */
        GAME_TRACK,
        /** 관리자, 또는 자기 트랙의 멘토·정회원. 요청에 트랙이 없어 어느 트랙 페이지에 쓸지는 트랙 API가 막는다. */
        OWN_TRACK
    }
}
