package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.RosterAddRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.RosterUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.RosterCandidateListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.RosterCandidateListResponse.RosterCandidateResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.RosterMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterRosterResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMemberId;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학기 명단 관리. 마감 개념이 없어 지난 학기도 추가·정정할 수 있다. 명단에서 빼는 기능은 없고,
 * 잘못 들어간 회원은 applicable=false(납부 비대상)로 둔다.
 */
@Service
@RequiredArgsConstructor
public class RosterService {

    private final DuesSemesterReader semesterReader;
    private final DuesSemesterWriter semesterWriter;
    private final DuesSemesterMemberRepository rosterRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public SemesterRosterResponse getRoster(String semesterId) {
        DuesSemester semester = semesterReader.get(semesterId);
        List<RosterMemberResponse> members = rosterRepository
                .findAllWithMemberBySemesterIdIn(List.of(semester.getId())).stream()
                .sorted(Comparator.comparing(DuesSemesterMember::getMember, DuesQueryService.MEMBER_ORDER))
                .map(RosterMemberResponse::from)
                .toList();
        return new SemesterRosterResponse(members);
    }

    @Transactional(readOnly = true)
    public RosterCandidateListResponse getCandidates(String semesterId) {
        DuesSemester semester = semesterReader.get(semesterId);
        List<RosterCandidateResponse> members = rosterRepository.findCandidates(semester.getId()).stream()
                .sorted(DuesQueryService.MEMBER_ORDER)
                .map(RosterCandidateResponse::from)
                .toList();
        return new RosterCandidateListResponse(members);
    }

    /** 회원 상태는 따지지 않는다(관리자가 판단). */
    @Transactional
    public RosterMemberResponse add(String semesterId, RosterAddRequest request) {
        DuesSemester semester = semesterReader.get(semesterId);
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.MEMBER_NOT_FOUND));
        if (rosterRepository.existsById(new DuesSemesterMemberId(semester.getId(), member.getId()))) {
            throw new LedgerException(LedgerExceptionType.ROSTER_MEMBER_EXISTS);
        }
        DuesSemesterMember row = semesterWriter.insertRosterRow(semester.getId(), member, request.applicable());
        return RosterMemberResponse.from(row);
    }

    /**
     * 납부 대상 여부 정정. 명단 행을 FOR UPDATE로 잠근다. 연결이 있는데 비대상으로 바꾸면 409인 검사는
     * 회비 연결(dues_link)이 생기는 PR2에서 이 잠금 아래에 더한다.
     */
    @Transactional
    public RosterMemberResponse update(String semesterId, Long memberId, RosterUpdateRequest request) {
        DuesSemester semester = semesterReader.get(semesterId);
        DuesSemesterMember row = rosterRepository.findForUpdate(semester.getId(), memberId)
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.NOT_ROSTER_MEMBER));
        row.updateApplicable(request.applicable());
        return RosterMemberResponse.from(row);
    }
}
