package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import static com.bcsdlab.bcsdinternalapiv2.ledger.repository.ConstraintViolations.isViolationOf;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesLink;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesLinkRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회비 연결 INSERT와 제약 위반 변환. 호출하는 쪽이 내역 행을 FOR UPDATE로 잠그고 기존 연결을 확인하므로 보통은
 * 위반이 나지 않는다. 그래도 나면 전역 핸들러의 400 대신 PK 위반은 409, 명단 FK 위반은 404로 바꾼다.
 */
@Component
@RequiredArgsConstructor
public class DuesLinkWriter {

    private static final String LINK_PRIMARY_KEY = "dues_link_pkey";
    private static final String LINK_ROSTER_FOREIGN_KEY = "fk_dues_link_semester_member";

    private final DuesLinkRepository linkRepository;

    @Transactional
    public DuesLink insert(Long entryId, Long semesterId, Long memberId, Long linkedBy, Instant linkedAt) {
        try {
            return linkRepository.saveAndFlush(new DuesLink(entryId, semesterId, memberId, linkedBy, linkedAt));
        } catch (DataIntegrityViolationException e) {
            if (isViolationOf(e, LINK_PRIMARY_KEY)) {
                throw new LedgerException(LedgerExceptionType.ENTRY_ALREADY_LINKED);
            }
            if (isViolationOf(e, LINK_ROSTER_FOREIGN_KEY)) {
                throw new LedgerException(LedgerExceptionType.NOT_ROSTER_MEMBER);
            }
            throw e;
        }
    }
}
