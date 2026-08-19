package com.bcsdlab.bcsdinternalapiv2.global.util;

import com.bcsdlab.bcsdinternalapiv2.global.exception.GlobalException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.GlobalExceptionType;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code display_order} 재부여 순수 함수. 특정 엔티티 타입에 의존하지 않는다 —
 * 트랙·커리큘럼 주차/토픽·활동 등 정렬 가능한 모든 도메인이 공유한다.
 *
 * <p>요청된 id 순서가 대상 id 집합과 정확히 일치하지 않으면(누락·추가·중복)
 * {@link GlobalException}을 던지고 아무 값도 반환하지 않는다 — 호출자가 반환값을 받기 전에는
 * 어떤 엔티티도 변경하지 않으므로 "부분 적용" 상태가 존재하지 않는다.
 */
public final class DisplayOrders {

    private DisplayOrders() {
    }

    /**
     * @param requestedIds 새 순서대로 나열된 id 목록 (배열 인덱스가 곧 새 display_order)
     * @param existingIds  현재 그 부모 아래 존재하는 전체 id 집합
     * @return id → 새 display_order(0-base) 매핑
     * @throws GlobalException requestedIds에 중복이 있거나, existingIds와 집합이 다를 때
     */
    public static Map<Long, Integer> reassign(List<Long> requestedIds, Collection<Long> existingIds) {
        Set<Long> requestedSet = new HashSet<>(requestedIds);
        if (requestedSet.size() != requestedIds.size()) {
            throw new GlobalException(GlobalExceptionType.ORDER_IDS_MISMATCH);
        }

        Set<Long> existingSet = new HashSet<>(existingIds);
        if (!requestedSet.equals(existingSet)) {
            throw new GlobalException(GlobalExceptionType.ORDER_IDS_MISMATCH);
        }

        Map<Long, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < requestedIds.size(); i++) {
            result.put(requestedIds.get(i), i);
        }
        return result;
    }
}
