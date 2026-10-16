package com.bcsdlab.bcsdinternalapiv2;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 통합 테스트 공통 베이스. Postgres 컨테이너를 테스트 클래스마다 새로 띄우지 않도록,
 * 정적 초기화 블록에서 JVM당 한 번만 기동하는 Testcontainers 공식 "싱글턴 컨테이너" 패턴을 쓴다.
 *
 * <p>{@code @Testcontainers}/{@code @Container}를 쓰지 않는 이유: 그 조합은 컨테이너를 테스트 클래스가
 * 끝날 때 내리는데, Spring은 같은 설정의 컨텍스트를 다음 클래스에서 재사용하므로 두 번째 클래스부터
 * 이미 내려간 컨테이너에 연결하려다 실패한다.
 *
 * <p>모든 통합 테스트가 같은 DB를 쓰므로, 테스트마다 마이그레이션이 넣는 기준 데이터를 뺀 전 테이블을
 * 비운다. 각 테스트의 {@code @BeforeEach}는 이 메서드 다음에 실행된다.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    /** Flyway 이력과 마이그레이션이 시드하는 기준 데이터 테이블. */
    private static final Set<String> PRESERVED_TABLES =
            Set.of("flyway_schema_history", "track", "position", "dues_exemption_reason");

    /**
     * 설정(목 빈, 프로퍼티)이 다른 테스트 클래스마다 Spring 컨텍스트가 따로 캐시되고 각자 커넥션 풀을 잡는다.
     * 모든 컨텍스트가 이 컨테이너 하나를 쓰므로 기본 연결 상한(100)을 넘지 않게 늘린다.
     */
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withCommand("postgres", "-c", "max_connections=500");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> "test-only-secret-key-not-for-production-32bytes-min");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetDatabase() {
        List<String> tables = jdbcTemplate.queryForList(
                "select tablename from pg_tables where schemaname = 'public'", String.class);
        String targets = String.join(", ", tables.stream().filter(t -> !PRESERVED_TABLES.contains(t)).toList());
        jdbcTemplate.execute("TRUNCATE " + targets + " CASCADE");
    }
}
