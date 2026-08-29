package com.bcsdlab.bcsdinternalapiv2.reservation;

import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.reservation.repository.ReservationGroupRepository;
import com.bcsdlab.bcsdinternalapiv2.reservation.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
abstract class ReservationIntegrationTestSupport {

    protected static final String RAW_PASSWORD = "Temp1234";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> "test-only-secret-key-not-for-production-32bytes-min");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected ReservationRepository reservationRepository;

    @Autowired
    protected ReservationGroupRepository reservationGroupRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @MockitoBean
    protected S3Presigner s3Presigner;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanUp() {
        reservationRepository.deleteAll();
        reservationGroupRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    protected Member createActiveMember(String studentNumber) {
        Member member = Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("예약테스트" + studentNumber)
                .track(Track.BACKEND)
                .generation("16")
                .memberType(MemberType.REGULAR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .email(studentNumber + "@bcsd.club")
                .phoneNumber("010" + studentNumber.substring(2))
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.MEMBER)
                .build();
        return memberRepository.save(member);
    }

    protected String login(String studentNumber) throws Exception {
        String loginBody = """
                {"studentNumber":"%s","password":"%s","rememberMe":false}
                """.formatted(studentNumber, RAW_PASSWORD);

        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }

    protected String createActiveMemberAndLogin(String studentNumber) throws Exception {
        createActiveMember(studentNumber);
        return login(studentNumber);
    }
}
