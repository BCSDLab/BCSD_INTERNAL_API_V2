package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.MemberDuesResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.MemberDues;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학기 회비 조회. 집계는 저장하지 않고 조회할 때마다 {@link DuesCalculator}로 계산한다.
 * 연결(PR2)과 면제(PR4)가 들어오기 전이라 지금은 둘 다 빈 목록으로 계산한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DuesQueryService {

    /** 이름, 학번, member_id 순. 동명이인이 있어도 순서가 고정된다. */
    static final Comparator<Member> MEMBER_ORDER = Comparator.comparing(Member::getName)
            .thenComparing(Member::getStudentNumber)
            .thenComparing(Member::getId);

    private final DuesSemesterRepository semesterRepository;
    private final DuesSemesterMemberRepository rosterRepository;
    private final DuesSemesterReader semesterReader;

    public SemesterDuesListResponse getSemesters() {
        List<DuesSemester> semesters = semesterRepository.findAllLatestFirst();
        Map<Long, List<DuesSemesterMember>> rowsBySemester = rosterRepository
                .findAllWithMemberBySemesterIdIn(semesters.stream().map(DuesSemester::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(row -> row.getId().getSemesterId()));
        List<SemesterDuesSummaryResponse> summaries = semesters.stream()
                .map(semester -> summarize(semester, rowsBySemester.getOrDefault(semester.getId(), List.of())))
                .toList();
        return new SemesterDuesListResponse(summaries);
    }

    public SemesterDuesDetailResponse getDetail(String semesterId) {
        DuesSemester semester = semesterReader.get(semesterId);
        List<DuesSemesterMember> rows = rosterOf(semester);
        List<MemberDues> dues = rows.stream().map(row -> calculate(semester, row)).toList();
        List<MemberDuesResponse> members = IntStream.range(0, rows.size())
                .mapToObj(i -> MemberDuesResponse.of(rows.get(i).getMember(), dues.get(i)))
                .toList();
        return new SemesterDuesDetailResponse(
                SemesterDuesSummaryResponse.of(semester, DuesCalculator.summarize(dues)), members);
    }

    public SemesterDuesSummaryResponse getSummary(DuesSemester semester) {
        return summarize(semester, rosterOf(semester));
    }

    private List<DuesSemesterMember> rosterOf(DuesSemester semester) {
        return rosterRepository.findAllWithMemberBySemesterIdIn(List.of(semester.getId())).stream()
                .sorted(Comparator.comparing(DuesSemesterMember::getMember, MEMBER_ORDER))
                .toList();
    }

    private SemesterDuesSummaryResponse summarize(DuesSemester semester, List<DuesSemesterMember> rows) {
        List<MemberDues> dues = rows.stream().map(row -> calculate(semester, row)).toList();
        return SemesterDuesSummaryResponse.of(semester, DuesCalculator.summarize(dues));
    }

    private MemberDues calculate(DuesSemester semester, DuesSemesterMember row) {
        // 면제는 PR4, 연결은 PR2에서 채운다.
        return DuesCalculator.calculate(semester.key(), semester.getMonthlyAmount(), row.isApplicable(),
                List.of(), List.of());
    }
}
