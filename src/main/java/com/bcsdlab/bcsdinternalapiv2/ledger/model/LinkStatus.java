package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 저장하지 않고 파생한다. 연결이 있으면 CONFIRMED, 없으면 DUES일 때 PENDING, 그 밖에는 NONE이다. */
public enum LinkStatus {
    CONFIRMED,
    PENDING,
    NONE;

    public static LinkStatus of(LedgerCategory category, boolean linked) {
        if (linked) {
            return CONFIRMED;
        }
        return category == LedgerCategory.DUES ? PENDING : NONE;
    }
}
