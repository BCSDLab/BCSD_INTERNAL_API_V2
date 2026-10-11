package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 알림 발송 전 조회(읽기 전용). 학기 명단 밖 회원이 하나라도 있으면 보내기 전에 전체를 거절한다. */
@Component
@RequiredArgsConstructor
public class DuesNotificationReader {

    private final DuesSemesterReader semesterReader;
    private final DuesSemesterMemberRepository rosterRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Context load(String semesterId, Collection<Long> memberIds, Long adminId) {
        DuesSemester semester = semesterReader.get(semesterId);
        Map<Long, DuesSemesterMember> roster = rosterRepository
                .findAllWithMemberBySemesterIdIn(List.of(semester.getId())).stream()
                .collect(Collectors.toMap(row -> row.getId().getMemberId(), row -> row));
        Map<Long, String> slackIds = new HashMap<>();
        for (Long memberId : memberIds) {
            DuesSemesterMember row = roster.get(memberId);
            if (row == null) {
                throw new LedgerException(LedgerExceptionType.NOT_ROSTER_MEMBER);
            }
            slackIds.put(memberId, row.getMember().getSlackId());
        }
        String adminSlackId = memberRepository.findById(adminId).map(Member::getSlackId).orElse(null);
        return new Context(semester.getId(), slackIds, adminSlackId, Set.copyOf(memberIds));
    }

    /** slackIds 값은 비어 있을 수 있다(인명부에 Slack ID 없음). */
    public record Context(Long semesterId, Map<Long, String> slackIds, String adminSlackId, Set<Long> memberIds) {
    }
}
