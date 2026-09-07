package com.bcsdlab.bcsdinternalapiv2.game.model;

/**
 * 게임물 내용정보 7종(INV-21). 값 목록이 법정 고시 항목이라 자유 문자열이 아니라
 * 고정된 키로 둔다. `game_rating`의 boolean 컬럼 7개와 1:1 대응한다.
 */
public enum GameContentDescriptor {
    SEXUALITY,
    VIOLENCE,
    FEAR,
    LANGUAGE,
    DRUGS,
    CRIME,
    GAMBLING,
}
