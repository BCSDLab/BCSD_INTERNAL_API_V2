package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemester;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesSemesterMember;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesSemesterRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.LedgerImportRepository;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 가져오기 미리보기용 조회(읽기 전용). */
@Component
@RequiredArgsConstructor
public class ImportReader {

    private final LedgerImportRepository importRepository;
    private final DuesSemesterRepository semesterRepository;
    private final DuesSemesterMemberRepository rosterRepository;

    @Transactional(readOnly = true)
    public PreviewContext load(Collection<String> rowKeys) {
        return new PreviewContext(importRepository.findExistingRowKeys(rowKeys), uniqueRosterNames());
    }

    /** 가장 최근 학기 명단에서 이름이 한 명뿐인 회원(이름 → 회원 id). 동명이인은 추천하지 않는다. */
    private Map<String, Long> uniqueRosterNames() {
        Optional<DuesSemester> latest = semesterRepository.findAllLatestFirst().stream().findFirst();
        if (latest.isEmpty()) {
            return Map.of();
        }
        List<DuesSemesterMember> rows = rosterRepository.findAllWithMemberBySemesterIdIn(List.of(latest.get().getId()));
        Map<String, List<Long>> byName = rows.stream().collect(Collectors.groupingBy(
                row -> Normalizer.normalize(row.getMember().getName().strip(), Normalizer.Form.NFC),
                Collectors.mapping(row -> row.getMember().getId(), Collectors.toList())));
        Map<String, Long> unique = new HashMap<>();
        byName.forEach((name, ids) -> {
            if (ids.size() == 1) {
                unique.put(name, ids.getFirst());
            }
        });
        return unique;
    }

    public record PreviewContext(Set<String> existingRowKeys, Map<String, Long> memberIdByName) {
    }
}
