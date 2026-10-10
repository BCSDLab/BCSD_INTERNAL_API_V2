package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * 회비·장부 통합 테스트 공통 베이스. 모든 하위 클래스가 같은 설정(고정 Clock)을 써서 Spring 컨텍스트를 하나만
 * 더 띄운다.
 *
 * <p>컨테이너를 다른 테스트 클래스와 공유하므로, 회비 테이블(회원 FK)을 남기면 다른 클래스의
 * memberRepository.deleteAll()이 FK 위반으로 실패한다. 그래서 테스트 앞뒤로 회비 테이블을 비운다.
 */
@Import(LedgerIntegrationTestSupport.TestClockConfig.class)
@TestPropertySource(properties = "app.mail.transport=log")
public abstract class LedgerIntegrationTestSupport extends IntegrationTestSupport {

    protected static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected TrackMasterRepository trackMasterRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected MutableClock clock;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected TrackMaster backend;
    protected String adminToken;

    @BeforeEach
    void setUpLedger() throws Exception {
        clearDuesTables();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        clock.setToday(LocalDate.of(2026, 10, 10));
        member("2020000000", "관리자").role(MemberRole.ADMIN).clubActive(false).save();
        adminToken = login("2020000000");
    }

    @AfterEach
    void tearDownLedger() {
        clearDuesTables();
    }

    private void clearDuesTables() {
        jdbcTemplate.update("DELETE FROM dues_semester_member");
        jdbcTemplate.update("DELETE FROM dues_semester");
    }

    protected MemberFixture member(String studentNumber, String name) {
        return new MemberFixture(studentNumber, name);
    }

    protected String login(String studentNumber) throws Exception {
        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentNumber":"%s","password":"%s","rememberMe":false}
                                """.formatted(studentNumber, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }

    protected ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return perform(request, adminToken);
    }

    protected ResultActions perform(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mockMvc.perform(request.header("Authorization", "Bearer " + token));
    }

    protected ResultActions perform(MockHttpServletRequestBuilder request, String token, String jsonBody)
            throws Exception {
        return perform(request.contentType(MediaType.APPLICATION_JSON).content(jsonBody), token);
    }

    protected ResultActions createSemester(int year, int term, long monthlyAmount) throws Exception {
        return perform(post("/v1/admin/dues/semesters"), adminToken,
                "{\"year\":%d,\"term\":%d,\"monthlyAmount\":%d}".formatted(year, term, monthlyAmount));
    }

    /** 기본값: 활동 중, ACTIVE, 납부 대상, 일반 회원. */
    protected class MemberFixture {

        private final String studentNumber;
        private final String name;
        private boolean clubActive = true;
        private boolean duesRequired = true;
        private MemberStatus status = MemberStatus.ACTIVE;
        private MemberRole role = MemberRole.MEMBER;

        private MemberFixture(String studentNumber, String name) {
            this.studentNumber = studentNumber;
            this.name = name;
        }

        public MemberFixture clubActive(boolean clubActive) {
            this.clubActive = clubActive;
            return this;
        }

        public MemberFixture duesRequired(boolean duesRequired) {
            this.duesRequired = duesRequired;
            return this;
        }

        public MemberFixture status(MemberStatus status) {
            this.status = status;
            return this;
        }

        public MemberFixture role(MemberRole role) {
            this.role = role;
            return this;
        }

        public Member save() {
            return memberRepository.save(Member.builder()
                    .studentNumber(studentNumber)
                    .password(passwordEncoder.encode(RAW_PASSWORD))
                    .name(name)
                    .track(backend)
                    .generation("24-하")
                    .memberType(MemberType.REGULAR)
                    .university("한국기술교육대학교")
                    .department("컴퓨터공학부")
                    .email(studentNumber + "@bcsd.club")
                    .clubActive(clubActive)
                    .duesRequired(duesRequired)
                    .status(status)
                    .role(role)
                    .build());
        }
    }

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock();
        }
    }
}
