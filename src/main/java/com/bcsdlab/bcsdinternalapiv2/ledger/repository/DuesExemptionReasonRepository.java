package com.bcsdlab.bcsdinternalapiv2.ledger.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 면제 사유. 이름 목록만 다루므로 엔티티 없이 SQL로 읽고 쓴다. 등록은 ON CONFLICT DO NOTHING이라
 * 같은 사유가 동시에 들어와도 하나만 생기고, 제약 위반 예외도 나지 않는다.
 */
@Repository
public class DuesExemptionReasonRepository {

    private final JdbcTemplate jdbcTemplate;

    public DuesExemptionReasonRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 시드 15개가 먼저(id 순), 그 뒤 등록 순. */
    public List<String> findAllNames() {
        return jdbcTemplate.queryForList("SELECT name FROM dues_exemption_reason ORDER BY id", String.class);
    }

    public boolean exists(String name) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM dues_exemption_reason WHERE name = ?)", Boolean.class, name));
    }

    public void insertIfAbsent(String name) {
        jdbcTemplate.update("INSERT INTO dues_exemption_reason (name) VALUES (?) ON CONFLICT (name) DO NOTHING", name);
    }
}
