package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 경로의 학기 ID("2026-2")로 학기를 찾는다. 형식이 틀리면 400, 없으면 404다. */
@Component
@RequiredArgsConstructor
public class DuesSemesterReader {

    private final DuesSemesterRepository semesterRepository;

    public DuesSemester get(String semesterId) {
        SemesterKey key = SemesterKey.parse(semesterId);
        return semesterRepository.findByYearAndTerm((short) key.year(), (short) key.term())
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.SEMESTER_NOT_FOUND));
    }
}
