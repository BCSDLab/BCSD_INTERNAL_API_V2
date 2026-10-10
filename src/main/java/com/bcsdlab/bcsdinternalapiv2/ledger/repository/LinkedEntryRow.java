package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;

/** 회비 집계 입력. 연결된 장부 기록의 (학기, 회원)과 방향·금액이다. */
public record LinkedEntryRow(Long semesterId, Long memberId, EntryType type, long amount) {
}
