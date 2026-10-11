package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 가져오기 전용 SQL. 중복 거래는 유니크 위반 예외를 잡지 않고 ON CONFLICT DO NOTHING으로 건너뛴다 —
 * PostgreSQL은 위반이 나면 트랜잭션 전체를 중단시키기 때문이다.
 */
@Repository
public class LedgerImportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public LedgerImportRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<String> findExistingRowKeys(Collection<String> rowKeys) {
        if (rowKeys.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(jdbcTemplate.queryForList(
                "SELECT row_key FROM ledger_entry WHERE row_key IN (:rowKeys)",
                new MapSqlParameterSource("rowKeys", rowKeys), String.class));
    }

    /** 새로 넣었으면 id, 이미 있던 거래면 빈 값. */
    public Optional<Long> insertIfAbsent(NewEntry entry) {
        List<Long> ids = jdbcTemplate.queryForList("""
                INSERT INTO ledger_entry (occurred_at, occurred_seq, type, category, counterparty, description, note,
                                          amount, bank_balance, source_file_name, row_key, created_by)
                VALUES (:occurredAt, :occurredSeq, :type, :category, :counterparty, :description, :note,
                        :amount, :bankBalance, :sourceFileName, :rowKey, :createdBy)
                ON CONFLICT (row_key) WHERE row_key IS NOT NULL DO NOTHING
                RETURNING id
                """, new MapSqlParameterSource()
                .addValue("occurredAt", Timestamp.valueOf(entry.occurredAt()))
                .addValue("occurredSeq", entry.occurredSeq())
                .addValue("type", entry.type())
                .addValue("category", entry.category())
                .addValue("counterparty", entry.counterparty())
                .addValue("description", entry.description())
                .addValue("note", entry.note())
                .addValue("amount", entry.amount())
                .addValue("bankBalance", entry.bankBalance())
                .addValue("sourceFileName", entry.sourceFileName())
                .addValue("rowKey", entry.rowKey())
                .addValue("createdBy", entry.createdBy()), Long.class);
        return ids.stream().findFirst();
    }

    public record NewEntry(LocalDateTime occurredAt, short occurredSeq, String type, String category,
                           String counterparty, String description, String note, long amount, long bankBalance,
                           String sourceFileName, String rowKey, Long createdBy) {
    }
}
