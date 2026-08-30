package com.bcsdlab.bcsdinternalapiv2.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.home.repository.MentorSlotRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminMentorSlotIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private MentorSlotRepository mentorSlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackMaster frontend;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        mentorSlotRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        frontend = trackMasterRepository.findByCode("FRONTEND").orElseThrow();

        Member admin = memberRepository.save(newMember("20271111", "admin@bcsd.club", MemberStatus.ACTIVE));
        adminToken = login("20271111");
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);
    }

    @Test
    @DisplayName("AC-10.1 이미 노출 중인 멤버를 다시 추가하면 409다")
    void 중복_추가는_409() throws Exception {
        Member mentor = memberRepository.save(newMember("20272222", "m1@bcsd.club", MemberStatus.ACTIVE));

        mockMvc.perform(post("/v1/admin/mentor-slots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":%d}".formatted(mentor.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/admin/mentor-slots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":%d}".formatted(mentor.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("AC-10.2 순서 변경이 공개 응답(GET /home)에 즉시 반영된다")
    void 순서_변경이_공개_응답에_반영된다() throws Exception {
        Member m1 = memberRepository.save(newMember("20273333", "m1@bcsd.club", MemberStatus.ACTIVE));
        Member m2 = memberRepository.save(newMember("20274444", "m2@bcsd.club", MemberStatus.ACTIVE));

        mockMvc.perform(post("/v1/admin/mentor-slots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":%d}".formatted(m1.getId())))
                .andExpect(status().isOk());
        String body = mockMvc.perform(post("/v1/admin/mentor-slots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":%d}".formatted(m2.getId())))
                .andReturn().getResponse().getContentAsString();

        // 순서 변경은 memberId가 아니라 슬롯 자체의 id로 한다 — 응답에 슬롯 id가 실제로
        // 노출되는지까지 함께 검증한다(프런트가 순서 변경을 걸 수 있으려면 필수).
        var slots = objectMapper.readTree(body);
        long slotId1 = slots.get(0).get("memberId").asLong() == m1.getId() ? slots.get(0).get("id").asLong()
                : slots.get(1).get("id").asLong();
        long slotId2 = slots.get(0).get("memberId").asLong() == m2.getId() ? slots.get(0).get("id").asLong()
                : slots.get(1).get("id").asLong();

        mockMvc.perform(patch("/v1/admin/mentor-slots/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(slotId2, slotId1)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/home"))
                .andExpect(jsonPath("$.mentors[0].name").value(m2.getName()))
                .andExpect(jsonPath("$.mentors[1].name").value(m1.getName()));
    }

    @Test
    @DisplayName("AC-10.3 멘토 슬롯에서 멤버를 제외해도 명부 데이터는 삭제되지 않는다")
    void 슬롯_제외는_명부를_건드리지_않는다() throws Exception {
        Member mentor = memberRepository.save(newMember("20275555", "m1@bcsd.club", MemberStatus.ACTIVE));
        mockMvc.perform(post("/v1/admin/mentor-slots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":%d}".formatted(mentor.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/v1/admin/mentor-slots/" + mentor.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/home")).andExpect(jsonPath("$.mentors.length()").value(0));
        assertThat(memberRepository.findById(mentor.getId())).isPresent();
    }

    @Test
    @DisplayName("AC-10.9 멘토 슬롯의 멤버가 WITHDRAWN이 되면 공개 응답에서 사라진다")
    void 탈퇴한_멘토는_공개_응답에서_제외된다() throws Exception {
        Member mentor = memberRepository.save(newMember("20276666", "m1@bcsd.club", MemberStatus.ACTIVE));
        mockMvc.perform(post("/v1/admin/mentor-slots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":%d}".formatted(mentor.getId())))
                .andExpect(status().isOk());

        mentor.withdraw();
        memberRepository.save(mentor);

        mockMvc.perform(get("/v1/home")).andExpect(jsonPath("$.mentors.length()").value(0));

        // 탈퇴 처리는 슬롯을 자동으로 지우지 않는다 — 다음 테스트의 member 정리가
        // FK 위반에 걸리지 않도록 이 테스트가 만든 슬롯은 직접 치운다.
        mentorSlotRepository.deleteAll();
    }

    private Member newMember(String studentNumber, String email, MemberStatus status) {
        return Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("테스트" + studentNumber)
                .track(frontend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email(email)
                .status(status)
                .build();
    }

    private String login(String studentNumber) throws Exception {
        String body = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentNumber\":\"%s\",\"password\":\"%s\",\"rememberMe\":false}"
                                .formatted(studentNumber, RAW_PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}
