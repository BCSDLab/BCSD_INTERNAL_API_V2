package com.bcsdlab.bcsdinternalapiv2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 통합 테스트 공통 베이스. Postgres 컨테이너를 테스트 클래스마다 새로 띄우지 않도록,
 * 정적 초기화 블록에서 JVM당 한 번만 기동하는 Testcontainers 공식 "싱글턴 컨테이너" 패턴을 쓴다.
 *
 * <p>{@code @Testcontainers}/{@code @Container}를 쓰지 않는 이유: 그 조합은 컨테이너 생명주기를
 * 테스트 클래스 단위로 관리해서(클래스마다 기동), 클래스가 늘어날수록 기동 비용이 누적된다.
 * 여기서는 정적 필드를 상속받는 모든 하위 클래스가 같은 컨테이너 인스턴스를 공유한다.
 *
 * <p>기존 7개 테스트 클래스(auth 4, member 2, 컨텍스트 로딩 1)는 각자 컨테이너를 띄우는 방식을
 * 그대로 쓴다 — 이 클래스로의 이전은 선택적 후속 작업이다(T-01).
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> "test-only-secret-key-not-for-production-32bytes-min");
    }

    @Autowired
    protected MockMvc mockMvc;
}
