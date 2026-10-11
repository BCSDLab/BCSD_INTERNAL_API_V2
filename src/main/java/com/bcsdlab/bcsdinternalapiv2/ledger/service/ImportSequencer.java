package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

/**
 * 같은 초에 일어난 거래의 실제 순서(occurred_seq)를 은행 잔액으로 복원한다(설계 5-5절).
 *
 * <p>실제 파일에서는 같은 초 거래의 행 순서가 잔액 흐름과 반대였다. 그래서 같은 거래일시 묶음 안에서
 * "앞 잔액 ± 다음 금액 = 다음 잔액"이 성립하는 순서를 찾는다. 직전 묶음의 마지막 잔액이 있으면 시작점으로 쓰고,
 * 그래도 맞는 순서가 없으면 시작점 없이 묶음 안에서만 찾는다. 끝내 없으면 관찰된 규칙(파일 순서의 역순)을 쓰고
 * 경고를 남긴다. 미리보기와 반영이 같은 함수를 써서, FE 계약에 순서 필드를 두지 않는다.
 */
@Slf4j
public final class ImportSequencer {

    /** 묶음이 이보다 크면 순열을 다 시도하지 않고 역순을 쓴다(실제 묶음은 2~3건). */
    private static final int MAX_PERMUTATION_SIZE = 6;

    private ImportSequencer() {
    }

    /**
     * 입력 순서(파일 순서)를 받아 (occurredAt, seq) 순서로 정렬된 결과를 돌려준다.
     * 거래일시가 같은 거래는 입력에서 이어져 있지 않아도 한 묶음으로 본다.
     */
    public static <T> List<Sequenced<T>> sequence(List<T> items, Function<T, Movement> movementOf) {
        List<T> sorted = new ArrayList<>(items);
        // 안정 정렬이라 같은 초 안에서는 파일 순서가 유지된다.
        sorted.sort((a, b) -> movementOf.apply(a).occurredAt().compareTo(movementOf.apply(b).occurredAt()));

        List<Sequenced<T>> result = new ArrayList<>(sorted.size());
        Long previousBalance = null;
        int i = 0;
        while (i < sorted.size()) {
            LocalDateTime at = movementOf.apply(sorted.get(i)).occurredAt();
            int end = i;
            while (end < sorted.size() && movementOf.apply(sorted.get(end)).occurredAt().equals(at)) {
                end++;
            }
            List<T> group = sorted.subList(i, end);
            List<T> ordered = group.size() == 1 ? group : orderGroup(group, movementOf, previousBalance);
            for (int seq = 0; seq < ordered.size(); seq++) {
                result.add(new Sequenced<>(ordered.get(seq), (short) seq));
            }
            previousBalance = movementOf.apply(ordered.getLast()).bankBalance();
            i = end;
        }
        return result;
    }

    private static <T> List<T> orderGroup(List<T> group, Function<T, Movement> movementOf, Long previousBalance) {
        List<T> reversed = new ArrayList<>(group);
        Collections.reverse(reversed);
        if (group.size() <= MAX_PERMUTATION_SIZE) {
            List<List<T>> candidates = new ArrayList<>();
            candidates.add(reversed);
            candidates.add(new ArrayList<>(group));
            permutations(new ArrayList<>(group), 0, candidates);
            for (Long start : previousBalance == null ? List.<Long>of() : List.of(previousBalance)) {
                for (List<T> candidate : candidates) {
                    if (chains(candidate, movementOf, start)) {
                        return candidate;
                    }
                }
            }
            for (List<T> candidate : candidates) {
                if (chains(candidate, movementOf, null)) {
                    return candidate;
                }
            }
        }
        log.warn("LEDGER_IMPORT_SEQUENCE_FALLBACK size={} — 잔액이 이어지는 순서를 찾지 못해 파일 순서의 역순을 쓴다",
                group.size());
        return reversed;
    }

    private static <T> boolean chains(List<T> order, Function<T, Movement> movementOf, Long start) {
        Long balance = start;
        for (T item : order) {
            Movement movement = movementOf.apply(item);
            if (balance != null && balance + movement.signedAmount() != movement.bankBalance()) {
                return false;
            }
            balance = movement.bankBalance();
        }
        return true;
    }

    private static <T> void permutations(List<T> items, int k, List<List<T>> out) {
        if (k == items.size()) {
            out.add(new ArrayList<>(items));
            return;
        }
        for (int i = k; i < items.size(); i++) {
            Collections.swap(items, k, i);
            permutations(items, k + 1, out);
            Collections.swap(items, k, i);
        }
    }

    /** 순서 판단에 쓰는 값. */
    public record Movement(LocalDateTime occurredAt, EntryType type, long amount, long bankBalance) {

        long signedAmount() {
            return type == EntryType.DEPOSIT ? amount : -amount;
        }
    }

    public record Sequenced<T>(T item, short seq) {
    }
}
