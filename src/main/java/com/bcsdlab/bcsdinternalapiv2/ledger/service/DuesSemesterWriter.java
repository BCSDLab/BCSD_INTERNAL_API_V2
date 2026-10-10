package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import static com.bcsdlab.bcsdinternalapiv2.ledger.repository.ConstraintViolations.isViolationOf;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학기·명단 INSERT와 유니크 위반의 409 변환. 호출하는 쪽이 먼저 존재 여부를 확인하지만, 확인과 저장 사이에
 * 다른 요청이 같은 행을 넣으면 여기서 제약 위반이 난다(전역 핸들러는 이를 400으로 응답하므로 409로 바꾼다).
 *
 * <p>학기는 IDENTITY라 save() 안에서 INSERT가 실행되고, 명단 행은 flush에서 실행된다. 그래서 save와 flush를
 * 함께 try 안에 둔다.
 */
@Component
@RequiredArgsConstructor
public class DuesSemesterWriter {

    private static final String SEMESTER_UNIQUE = "uk_dues_semester_year_term";
    private static final String ROSTER_PRIMARY_KEY = "dues_semester_member_pkey";

    private final DuesSemesterRepository semesterRepository;
    private final DuesSemesterMemberRepository rosterRepository;

    @Transactional
    public DuesSemester insertSemester(SemesterKey key, long monthlyAmount, Long createdBy) {
        try {
            return semesterRepository.saveAndFlush(new DuesSemester(key, monthlyAmount, createdBy));
        } catch (DataIntegrityViolationException e) {
            if (isViolationOf(e, SEMESTER_UNIQUE)) {
                throw new LedgerException(LedgerExceptionType.SEMESTER_ALREADY_EXISTS);
            }
            throw e;
        }
    }

    @Transactional
    public DuesSemesterMember insertRosterRow(Long semesterId, Member member, boolean applicable) {
        try {
            return rosterRepository.saveAndFlush(new DuesSemesterMember(semesterId, member, applicable));
        } catch (DataIntegrityViolationException e) {
            if (isViolationOf(e, ROSTER_PRIMARY_KEY)) {
                throw new LedgerException(LedgerExceptionType.ROSTER_MEMBER_EXISTS);
            }
            throw e;
        }
    }
}
