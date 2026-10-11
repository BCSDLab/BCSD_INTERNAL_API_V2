package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.NotificationFailureReason;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesNotificationRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesNotificationRepository.BatchRow;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 발송 기록 쓰기. Slack 호출(네트워크)은 서비스가 트랜잭션 밖에서 하고, 회원마다 결과를 이 빈의 별도 트랜잭션으로
 * 바로 커밋한다 — 중간에 실패해도 이미 보낸 회원이 SENT로 남아 같은 키로 다시 요청할 때 건너뛴다.
 */
@Component
@RequiredArgsConstructor
public class DuesNotificationWriter {

    private final DuesNotificationRepository repository;

    /**
     * 같은 키의 발송 건을 시작하거나 이어서 처리한다. 돌려주는 값은 이미 보낸(SENT) 회원이다.
     * 같은 키에 내용이 다르면 409 IDEMPOTENCY_KEY_REUSED, 아직 보내는 중이면 409 NOTIFICATION_IN_PROGRESS다.
     */
    @Transactional
    public Started start(String key, String requestHash, Long semesterId, String type, Long adminId,
                         List<Long> memberIds) {
        Optional<BatchRow> existing = repository.findBatchByKeyForUpdate(key);
        if (existing.isPresent()) {
            BatchRow batch = existing.get();
            if (!batch.requestHash().equals(requestHash)) {
                throw new LedgerException(LedgerExceptionType.IDEMPOTENCY_KEY_REUSED);
            }
            if ("IN_PROGRESS".equals(batch.status()) && batch.recentlyUpdated()) {
                throw new LedgerException(LedgerExceptionType.NOTIFICATION_IN_PROGRESS);
            }
            repository.updateBatchStatus(batch.id(), "IN_PROGRESS");
            Set<Long> sent = repository.findRecipients(batch.id()).stream()
                    .filter(row -> "SENT".equals(row.status()))
                    .map(DuesNotificationRepository.RecipientRow::memberId)
                    .collect(Collectors.toSet());
            return new Started(batch.id(), sent);
        }
        long batchId;
        try {
            batchId = repository.insertBatch(key, requestHash, semesterId, type, false, adminId, memberIds.size());
        } catch (DuplicateKeyException e) {
            // 확인과 저장 사이에 같은 키의 요청이 먼저 시작했다.
            throw new LedgerException(LedgerExceptionType.NOTIFICATION_IN_PROGRESS);
        }
        memberIds.forEach(memberId -> repository.insertRecipient(batchId, memberId));
        return new Started(batchId, Set.of());
    }

    /** 나에게 테스트 발송. 요청 키 없이 test로 남겨 중복 판정·회원별 집계에 섞이지 않는다. 수신자는 관리자 본인이다. */
    @Transactional
    public long startTest(String requestHash, Long semesterId, String type, Long adminId) {
        long batchId = repository.insertBatch(null, requestHash, semesterId, type, true, adminId, 1);
        repository.insertRecipient(batchId, adminId);
        return batchId;
    }

    @Transactional
    public void markSent(long batchId, Long memberId, String channel, String ts) {
        repository.markSent(batchId, memberId, channel, ts);
    }

    @Transactional
    public void markFailed(long batchId, Long memberId, NotificationFailureReason reason) {
        repository.markFailed(batchId, memberId, reason);
    }

    @Transactional
    public void finish(long batchId) {
        repository.updateBatchStatus(batchId, "DONE");
    }

    public record Started(long batchId, Set<Long> alreadySent) {
    }
}
