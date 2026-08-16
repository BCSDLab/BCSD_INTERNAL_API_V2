package com.bcsdlab.bcsdinternalapiv2.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.auth.model.RefreshToken;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class RefreshTokenIntegrationTest {

    private static final String RAW_PASSWORD = "Temp1234";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> "test-only-secret-key-not-for-production-32bytes-min");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 회전_경합에서_승자의_새_토큰도_재사용_탐지시_함께_폐기된다() throws Exception {
        Member activeMember = Member.builder()
                .studentNumber("20237777")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("이민수")
                .track(Track.BACKEND)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("rotation-race@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(activeMember);

        String loginBody = """
                {"studentNumber":"20237777","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        Cookie liveRefreshCookie = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("refreshToken");

        int concurrentAttempts = 2;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentAttempts);
        List<Integer> statusCodes;
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < concurrentAttempts; i++) {
                futures.add(executor.submit(() -> mockMvc.perform(post("/v1/auth/reissue")
                                .cookie(liveRefreshCookie))
                        .andReturn()
                        .getResponse()
                        .getStatus()));
            }
            statusCodes = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statusCodes.add(future.get(10, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        }

        assertThat(statusCodes).filteredOn(status -> status == 200).hasSize(1);
        assertThat(statusCodes).filteredOn(status -> status == 401).hasSize(1);

        List<RefreshToken> tokens = refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(activeMember.getId());
        assertThat(tokens).isEmpty();
    }

    @Test
    void 동시에_들어온_리프레시_토큰_재사용_공격은_커넥션_풀을_고갈시키지_않는다() throws Exception {
        Member activeMember = Member.builder()
                .studentNumber("20238888")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("박영희")
                .track(Track.BACKEND)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("reuse-attack@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(activeMember);

        String loginBody = """
                {"studentNumber":"20238888","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        Cookie staleRefreshCookie = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("refreshToken");

        mockMvc.perform(post("/v1/auth/reissue").cookie(staleRefreshCookie))
                .andExpect(status().isOk());

        int concurrentAttempts = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentAttempts);
        List<Integer> statusCodes;
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < concurrentAttempts; i++) {
                futures.add(executor.submit(() -> mockMvc.perform(post("/v1/auth/reissue")
                                .cookie(staleRefreshCookie))
                        .andReturn()
                        .getResponse()
                        .getStatus()));
            }
            statusCodes = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statusCodes.add(future.get(10, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        }

        assertThat(statusCodes).allMatch(status -> status == 401);

        List<RefreshToken> tokens = refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(activeMember.getId());
        assertThat(tokens).isEmpty();
    }

    @Test
    void 재사용된_리프레시_토큰이_감지되면_해당_회원의_모든_토큰이_폐기된다() throws Exception {
        Member activeMember = Member.builder()
                .studentNumber("20239999")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("김철수")
                .track(Track.BACKEND)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("active@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(activeMember);

        String loginBody = """
                {"studentNumber":"20239999","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        Cookie firstRefreshCookie = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("refreshToken");

        Cookie rotatedRefreshCookie = mockMvc.perform(post("/v1/auth/reissue")
                        .cookie(firstRefreshCookie))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("refreshToken");

        assertThat(rotatedRefreshCookie).isNotNull();

        mockMvc.perform(post("/v1/auth/reissue")
                        .cookie(firstRefreshCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("보안상의 이유로 로그아웃되었습니다. 다시 로그인해 주세요."));

        List<RefreshToken> tokens = refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(activeMember.getId());
        assertThat(tokens).isEmpty();
    }
}
