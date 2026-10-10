package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;

/** creatable이 false여도 무엇이 막혔는지 보여 줄 수 있게 nextSemester를 내려준다. */
public record SemesterCreatableResponse(
        String currentSemesterId,
        NextSemester nextSemester,
        boolean creatable
) {

    public static SemesterCreatableResponse of(SemesterKey current, SemesterKey next, boolean creatable) {
        return new SemesterCreatableResponse(current.id(), new NextSemester(next.year(), next.term()), creatable);
    }

    public record NextSemester(int year, int term) {
    }
}
