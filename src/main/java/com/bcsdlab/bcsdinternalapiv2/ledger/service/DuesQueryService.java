package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.MemberDuesResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMemberId;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesLinkRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.LinkedAmount;
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
 * 면제(PR4)가 들어오기 전이라 면제는 빈 목록으로 계산한다.
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
    private final DuesLinkRepository linkRepository;
    private final DuesSemesterReader semesterReader;

    public SemesterDuesListResponse getSemesters() {
        List<DuesSemester> semesters = semesterRepository.findAllLatestFirst();
        List<Long> semesterIds = semesters.stream().map(DuesSemester::getId).toList();
        Map<Long, List<DuesSemesterMember>> rowsBySemester = rosterRepository
                .findAllWithMemberBySemesterIdIn(semesterIds)
                .stream()
                .collect(Collectors.groupingBy(row -> row.getId().getSemesterId()));
        Map<DuesSemesterMemberId, List<LinkedAmount>> links = linksOf(semesterIds);
        List<SemesterDuesSummaryResponse> summaries = semesters.stream()
                .map(semester -> summarize(semester, rowsBySemester.getOrDefault(semester.getId(), List.of()), links))
                .toList();
        return new SemesterDuesListResponse(summaries);
    }

    public SemesterDuesDetailResponse getDetail(String semesterId) {
        DuesSemester semester = semesterReader.get(semesterId);
        List<DuesSemesterMember> rows = rosterOf(semester);
        Map<DuesSemesterMemberId, List<LinkedAmount>> links = linksOf(List.of(semester.getId()));
        List<MemberDues> dues = rows.stream().map(row -> calculate(semester, row, links)).toList();
        List<MemberDuesResponse> members = IntStream.range(0, rows.size())
                .mapToObj(i -> MemberDuesResponse.of(rows.get(i).getMember(), dues.get(i)))
                .toList();
        return new SemesterDuesDetailResponse(
                SemesterDuesSummaryResponse.of(semester, DuesCalculator.summarize(dues)), members);
    }

    public SemesterDuesSummaryResponse getSummary(DuesSemester semester) {
        return summarize(semester, rosterOf(semester), linksOf(List.of(semester.getId())));
    }

    private List<DuesSemesterMember> rosterOf(DuesSemester semester) {
        return rosterRepository.findAllWithMemberBySemesterIdIn(List.of(semester.getId())).stream()
                .sorted(Comparator.comparing(DuesSemesterMember::getMember, MEMBER_ORDER))
                .toList();
    }

    /** 학기들에 연결된 장부 기록을 (학기, 회원)별로 한 번에 읽는다. */
    private Map<DuesSemesterMemberId, List<LinkedAmount>> linksOf(List<Long> semesterIds) {
        if (semesterIds.isEmpty()) {
            return Map.of();
        }
        return linkRepository.findLinkedEntriesBySemesterIdIn(semesterIds).stream()
                .collect(Collectors.groupingBy(
                        row -> new DuesSemesterMemberId(row.semesterId(), row.memberId()),
                        Collectors.mapping(row -> new LinkedAmount(row.type(), row.amount()), Collectors.toList())));
    }

    private SemesterDuesSummaryResponse summarize(DuesSemester semester, List<DuesSemesterMember> rows,
                                                  Map<DuesSemesterMemberId, List<LinkedAmount>> links) {
        List<MemberDues> dues = rows.stream().map(row -> calculate(semester, row, links)).toList();
        return SemesterDuesSummaryResponse.of(semester, DuesCalculator.summarize(dues));
    }

    private MemberDues calculate(DuesSemester semester, DuesSemesterMember row,
                                 Map<DuesSemesterMemberId, List<LinkedAmount>> links) {
        // 면제는 PR4에서 채운다.
        return DuesCalculator.calculate(semester.key(), semester.getMonthlyAmount(), row.isApplicable(),
                List.of(), links.getOrDefault(row.getId(), List.of()));
    }
}
