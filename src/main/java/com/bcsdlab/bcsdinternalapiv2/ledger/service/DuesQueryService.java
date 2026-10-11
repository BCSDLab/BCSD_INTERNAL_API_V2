package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.ExemptionDraft;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionPreviewResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.MemberDuesResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesExemption;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMemberId;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesExemptionRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesLinkRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.ExemptionPeriod;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.LinkedAmount;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.MemberDues;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.ArrayList;
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
 * 입력은 (학기, 회원)별 연결된 입출금과 회원별 면제다.
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
    private final DuesExemptionRepository exemptionRepository;
    private final MemberRepository memberRepository;
    private final DuesSemesterReader semesterReader;

    public SemesterDuesListResponse getSemesters() {
        List<DuesSemester> semesters = semesterRepository.findAllLatestFirst();
        List<Long> semesterIds = semesters.stream().map(DuesSemester::getId).toList();
        List<DuesSemesterMember> allRows = rosterRepository.findAllWithMemberBySemesterIdIn(semesterIds);
        Map<Long, List<DuesSemesterMember>> rowsBySemester = allRows.stream()
                .collect(Collectors.groupingBy(row -> row.getId().getSemesterId()));
        Inputs inputs = new Inputs(linksOf(semesterIds), exemptionsOf(allRows));
        List<SemesterDuesSummaryResponse> summaries = semesters.stream()
                .map(semester -> summarize(semester, rowsBySemester.getOrDefault(semester.getId(), List.of()),
                        inputs))
                .toList();
        return new SemesterDuesListResponse(summaries);
    }

    public SemesterDuesDetailResponse getDetail(String semesterId) {
        DuesSemester semester = semesterReader.get(semesterId);
        List<DuesSemesterMember> rows = rosterOf(semester);
        Inputs inputs = inputsOf(semester, rows);
        List<MemberDues> dues = rows.stream().map(row -> calculate(semester, row, inputs)).toList();
        List<MemberDuesResponse> members = IntStream.range(0, rows.size())
                .mapToObj(i -> MemberDuesResponse.of(rows.get(i).getMember(), dues.get(i)))
                .toList();
        return new SemesterDuesDetailResponse(
                SemesterDuesSummaryResponse.of(semester, DuesCalculator.summarize(dues)), members);
    }

    public SemesterDuesSummaryResponse getSummary(DuesSemester semester) {
        List<DuesSemesterMember> rows = rosterOf(semester);
        return summarize(semester, rows, inputsOf(semester, rows));
    }

    /**
     * 면제 저장 전 미리보기. DB에 아무것도 쓰지 않는다(사유 등록도 하지 않는다). after는 그 회원의 면제 목록에서
     * editingId 면제를 초안으로 바꾸거나 초안을 더해 같은 규칙으로 계산한다. 초안의 id는 editingId, 새 면제면 -1이다.
     */
    public ExemptionPreviewResponse preview(String semesterId, ExemptionDraft draft, Long editingId) {
        DuesSemester semester = semesterReader.get(semesterId);
        if (editingId != null && !exemptionRepository.existsById(editingId)) {
            throw new LedgerException(LedgerExceptionType.EXEMPTION_NOT_FOUND);
        }
        if (!draft.hasValidPeriod()) {
            throw new LedgerException(LedgerExceptionType.INVALID_EXEMPTION_PERIOD);
        }
        if (!memberRepository.existsById(draft.memberId())) {
            throw new LedgerException(LedgerExceptionType.MEMBER_NOT_FOUND);
        }
        DuesSemesterMember row = rosterOf(semester).stream()
                .filter(candidate -> candidate.getId().getMemberId().equals(draft.memberId()))
                .findFirst()
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.NOT_ROSTER_MEMBER));

        List<LinkedAmount> links = linksOf(List.of(semester.getId())).getOrDefault(row.getId(), List.of());
        List<ExemptionPeriod> current = exemptionRepository.findAllByMemberIdIn(List.of(draft.memberId())).stream()
                .map(DuesQueryService::toPeriod)
                .toList();
        List<ExemptionPeriod> next = new ArrayList<>(current.stream()
                .filter(exemption -> editingId == null || exemption.id() != editingId)
                .toList());
        next.add(new ExemptionPeriod(editingId == null ? -1 : editingId, draft.reason(), draft.startMonth(),
                draft.endMonth()));

        MemberDues before = DuesCalculator.calculate(semester.key(), semester.getMonthlyAmount(), row.isApplicable(),
                current, links);
        MemberDues after = DuesCalculator.calculate(semester.key(), semester.getMonthlyAmount(), row.isApplicable(),
                next, links);
        return new ExemptionPreviewResponse(MemberDuesResponse.of(row.getMember(), before),
                MemberDuesResponse.of(row.getMember(), after));
    }

    private List<DuesSemesterMember> rosterOf(DuesSemester semester) {
        return rosterRepository.findAllWithMemberBySemesterIdIn(List.of(semester.getId())).stream()
                .sorted(Comparator.comparing(DuesSemesterMember::getMember, MEMBER_ORDER))
                .toList();
    }

    private Inputs inputsOf(DuesSemester semester, List<DuesSemesterMember> rows) {
        return new Inputs(linksOf(List.of(semester.getId())), exemptionsOf(rows));
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

    /** 명단 회원들의 면제를 회원별로 한 번에 읽는다. 학기 밖의 달은 계산기가 걸러 낸다. */
    private Map<Long, List<ExemptionPeriod>> exemptionsOf(List<DuesSemesterMember> rows) {
        List<Long> memberIds = rows.stream().map(row -> row.getId().getMemberId()).distinct().toList();
        if (memberIds.isEmpty()) {
            return Map.of();
        }
        return exemptionRepository.findAllByMemberIdIn(memberIds).stream()
                .collect(Collectors.groupingBy(DuesExemption::getMemberId,
                        Collectors.mapping(DuesQueryService::toPeriod, Collectors.toList())));
    }

    private static ExemptionPeriod toPeriod(DuesExemption exemption) {
        return new ExemptionPeriod(exemption.getId(), exemption.getReason(), exemption.getStartMonth(),
                exemption.getEndMonth());
    }

    private SemesterDuesSummaryResponse summarize(DuesSemester semester, List<DuesSemesterMember> rows,
                                                  Inputs inputs) {
        List<MemberDues> dues = rows.stream().map(row -> calculate(semester, row, inputs)).toList();
        return SemesterDuesSummaryResponse.of(semester, DuesCalculator.summarize(dues));
    }

    private MemberDues calculate(DuesSemester semester, DuesSemesterMember row, Inputs inputs) {
        return DuesCalculator.calculate(semester.key(), semester.getMonthlyAmount(), row.isApplicable(),
                inputs.exemptions().getOrDefault(row.getId().getMemberId(), List.of()),
                inputs.links().getOrDefault(row.getId(), List.of()));
    }

    /** 집계 입력: (학기, 회원)별 연결과 회원별 면제. */
    private record Inputs(Map<DuesSemesterMemberId, List<LinkedAmount>> links,
                          Map<Long, List<ExemptionPeriod>> exemptions) {
    }
}
