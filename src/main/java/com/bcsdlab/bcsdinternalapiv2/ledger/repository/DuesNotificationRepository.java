package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.NotificationBatchStatus;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.NotificationFailureReason;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.NotificationRecipientStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** 회비 알림 발송 기록(V26). 본문은 저장하지 않는다. */
@Repository
public class DuesNotificationRepository {

    /** 이 시간 안에 갱신된 IN_PROGRESS 발송 건은 아직 보내는 중으로 본다. 넘으면 서버가 멈춘 것으로 보고 이어서 처리한다. */
    private static final String IN_PROGRESS_WINDOW = "60 seconds";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DuesNotificationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insertBatch(String idempotencyKey, String requestHash, Long semesterId, String type, boolean testToSelf,
                            Long requestedBy, int recipientCount) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO dues_notification_batch (idempotency_key, request_hash, semester_id, type, test_to_self,
                                                     status, requested_by, recipient_count)
                VALUES (:key, :hash, :semesterId, :type, :test, 'IN_PROGRESS', :requestedBy, :count)
                RETURNING id
                """, new MapSqlParameterSource()
                .addValue("key", idempotencyKey)
                .addValue("hash", requestHash)
                .addValue("semesterId", semesterId)
                .addValue("type", type)
                .addValue("test", testToSelf)
                .addValue("requestedBy", requestedBy)
                .addValue("count", recipientCount), Long.class);
    }

    /** 같은 키의 재요청이 동시에 이어서 처리하지 않도록 행을 잠근다. */
    public Optional<BatchRow> findBatchByKeyForUpdate(String idempotencyKey) {
        List<BatchRow> rows = jdbcTemplate.query("""
                SELECT id, request_hash, status, updated_at > now() - CAST(:window AS INTERVAL) AS recently_updated
                FROM dues_notification_batch WHERE idempotency_key = :key FOR UPDATE
                """, new MapSqlParameterSource().addValue("key", idempotencyKey).addValue("window", IN_PROGRESS_WINDOW),
                (rs, i) -> new BatchRow(rs.getLong("id"), rs.getString("request_hash").strip(),
                        NotificationBatchStatus.valueOf(rs.getString("status")), rs.getBoolean("recently_updated")));
        return rows.stream().findFirst();
    }

    public void updateBatchStatus(long batchId, NotificationBatchStatus status) {
        jdbcTemplate.update("UPDATE dues_notification_batch SET status = :status, updated_at = now() WHERE id = :id",
                new MapSqlParameterSource().addValue("status", status.name()).addValue("id", batchId));
    }

    public void insertRecipient(long batchId, Long memberId) {
        jdbcTemplate.update("""
                INSERT INTO dues_notification_recipient (batch_id, member_id, status) VALUES (:batchId, :memberId, 'PENDING')
                """, new MapSqlParameterSource().addValue("batchId", batchId).addValue("memberId", memberId));
    }

    public List<RecipientRow> findRecipients(long batchId) {
        return jdbcTemplate.query("""
                SELECT member_id, status, failure_reason FROM dues_notification_recipient WHERE batch_id = :batchId
                """, new MapSqlParameterSource("batchId", batchId),
                (rs, i) -> new RecipientRow(rs.getLong("member_id"),
                        NotificationRecipientStatus.valueOf(rs.getString("status")),
                        rs.getString("failure_reason") == null ? null
                                : NotificationFailureReason.valueOf(rs.getString("failure_reason"))));
    }

    public void markSent(long batchId, Long memberId, String channel, String ts) {
        jdbcTemplate.update("""
                UPDATE dues_notification_recipient
                SET status = 'SENT', failure_reason = NULL, slack_channel = :channel, slack_ts = :ts,
                    sent_at = now(), updated_at = now()
                WHERE batch_id = :batchId AND member_id = :memberId
                """, new MapSqlParameterSource().addValue("channel", channel).addValue("ts", ts)
                .addValue("batchId", batchId).addValue("memberId", memberId));
        touch(batchId);
    }

    public void markFailed(long batchId, Long memberId, NotificationFailureReason reason) {
        jdbcTemplate.update("""
                UPDATE dues_notification_recipient
                SET status = 'FAILED', failure_reason = :reason, updated_at = now()
                WHERE batch_id = :batchId AND member_id = :memberId
                """, new MapSqlParameterSource().addValue("reason", reason.name())
                .addValue("batchId", batchId).addValue("memberId", memberId));
        touch(batchId);
    }

    private void touch(long batchId) {
        jdbcTemplate.update("UPDATE dues_notification_batch SET updated_at = now() WHERE id = :id",
                new MapSqlParameterSource("id", batchId));
    }

    public record BatchRow(long id, String requestHash, NotificationBatchStatus status, boolean recentlyUpdated) {
    }

    public record RecipientRow(long memberId, NotificationRecipientStatus status, NotificationFailureReason failureReason) {
    }
}
