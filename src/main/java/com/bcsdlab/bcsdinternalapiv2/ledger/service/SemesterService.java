package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.SemesterCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterCreatableResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학기 생성. 만들 수 있는 학기는 가장 최근 학기의 다음 학기(학기가 없으면 현재 학기)이고, 그 학기가
 * 현재 학기의 바로 다음 학기보다 늦으면 만들 수 없다. 현재 학기는 Asia/Seoul 날짜로 정한다.
 */
@Service
@RequiredArgsConstructor
public class SemesterService {

    private final Clock clock;
    private final DuesSemesterRepository semesterRepository;
    private final DuesSemesterMemberRepository rosterRepository;
    private final DuesSemesterWriter semesterWriter;
    private final DuesQueryService duesQueryService;

    @Transactional(readOnly = true)
    public SemesterCreatableResponse getCreatable() {
        Creatable creatable = creatable();
        return SemesterCreatableResponse.of(creatable.current(), creatable.next(), creatable.creatable());
    }

    @Transactional
    public SemesterDuesSummaryResponse create(SemesterCreateRequest request, Long adminId) {
        SemesterKey requested = new SemesterKey(request.year(), request.term());
        if (semesterRepository.existsByYearAndTerm((short) requested.year(), (short) requested.term())) {
            throw new LedgerException(LedgerExceptionType.SEMESTER_ALREADY_EXISTS);
        }
        Creatable creatable = creatable();
        if (!creatable.creatable() || !creatable.next().equals(requested)) {
            throw new LedgerException(LedgerExceptionType.SEMESTER_NOT_CREATABLE);
        }
        DuesSemester semester = semesterWriter.insertSemester(requested, request.monthlyAmount(), adminId);
        rosterRepository.insertSnapshot(semester.getId());
        return duesQueryService.getSummary(semester);
    }

    private Creatable creatable() {
        SemesterKey current = SemesterKey.of(LocalDate.now(clock));
        SemesterKey next = semesterRepository.findAllLatestFirst().stream()
                .findFirst()
                .map(latest -> latest.key().next())
                .orElse(current);
        return new Creatable(current, next, next.order() <= current.next().order());
    }

    private record Creatable(SemesterKey current, SemesterKey next, boolean creatable) {
    }
}
