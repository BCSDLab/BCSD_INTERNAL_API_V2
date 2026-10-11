package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 가져오기에서 고를 수 있는 분류(FE ImportCategory). 회비 회원이 추천되면 DUES, 그 밖에는 ETC다. */
public enum ImportCategory {
    DUES,
    ETC;

    public LedgerCategory toLedgerCategory() {
        return this == DUES ? LedgerCategory.DUES : LedgerCategory.ETC;
    }
}
