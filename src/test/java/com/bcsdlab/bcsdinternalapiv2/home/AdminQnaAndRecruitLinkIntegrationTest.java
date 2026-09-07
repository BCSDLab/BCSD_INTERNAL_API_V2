package com.bcsdlab.bcsdinternalapiv2.home;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.home.repository.MentorSlotRepository;
import com.bcsdlab.bcsdinternalapiv2.home.repository.QnaItemRepository;
import com.bcsdlab.bcsdinternalapiv2.home.repository.RecruitLinkHistoryRepository;
import com.bcsdlab.bcsdinternalapiv2.home.repository.RecruitLinkRepository;
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

class AdminQnaAndRecruitLinkIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private QnaItemRepository qnaItemRepository;

    @Autowired
    private MentorSlotRepository mentorSlotRepository;

    @Autowired
    private RecruitLinkRepository recruitLinkRepository;

    @Autowired
    private RecruitLinkHistoryRepository recruitLinkHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        qnaItemRepository.deleteAll();
        recruitLinkHistoryRepository.deleteAll();
        recruitLinkRepository.deleteAll();
        mentorSlotRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        TrackMaster frontend = trackMasterRepository.findByCode("FRONTEND").orElseThrow();

        Member admin = memberRepository.save(Member.builder()
                .studentNumber("20281111")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("테스트")
                .track(frontend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("admin@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build());
        adminToken = login("20281111");
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);
    }

    @Test
    @DisplayName("AC-10.4 숨긴 질문은 공개 응답에서 사라지지만 관리자 목록에는 남는다")
    void 숨긴_질문은_공개에서만_사라진다() throws Exception {
        String body = mockMvc.perform(post("/v1/admin/qna")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"지원 자격이 어떻게 되나요?\",\"answer\":\"누구나 가능합니다.\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(patch("/v1/admin/qna/" + id + "/publish")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPublished\":false}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/home")).andExpect(jsonPath("$.qna.length()").value(0));
        mockMvc.perform(get("/v1/admin/qna").header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("AC-10.5 순서 변경이 공개 응답 순서에 반영된다")
    void 순서_변경이_공개_응답에_반영된다() throws Exception {
        String body1 = mockMvc.perform(post("/v1/admin/qna")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Q1\",\"answer\":\"A1\"}"))
                .andReturn().getResponse().getContentAsString();
        String body2 = mockMvc.perform(post("/v1/admin/qna")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Q2\",\"answer\":\"A2\"}"))
                .andReturn().getResponse().getContentAsString();
        long id1 = objectMapper.readTree(body1).get("id").asLong();
        long id2 = objectMapper.readTree(body2).get("id").asLong();

        mockMvc.perform(patch("/v1/admin/qna/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(id2, id1)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/home"))
                .andExpect(jsonPath("$.qna[0].question").value("Q2"))
                .andExpect(jsonPath("$.qna[1].question").value("Q1"));
    }

    @Test
    @DisplayName("AC-10.6 forms.gle·docs.google.com/forms가 아닌 URL은 400이다")
    void 잘못된_구글폼_URL은_400() throws Exception {
        mockMvc.perform(put("/v1/admin/recruit-link")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"googleFormUrl\":\"https://evil.com/form\",\"isOpen\":true,"
                                + "\"closedMessage\":\"모집 예정\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AC-10.7 모집 링크를 저장하면 변경 이력이 남는다")
    void 저장하면_이력이_남는다() throws Exception {
        mockMvc.perform(put("/v1/admin/recruit-link")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"googleFormUrl\":\"https://forms.gle/abcdefg\",\"isOpen\":true,"
                                + "\"closedMessage\":\"모집 예정\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.googleFormUrl").value("https://forms.gle/abcdefg"));

        mockMvc.perform(get("/v1/admin/recruit-link/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].googleFormUrl").value("https://forms.gle/abcdefg"));
    }

    @Test
    @DisplayName("AC-10.8 isOpen을 false로 바꾸면 공개 응답의 버튼 문구가 closedMessage로 바뀐다")
    void isOpen_false면_closedMessage가_노출된다() throws Exception {
        mockMvc.perform(put("/v1/admin/recruit-link")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"googleFormUrl\":\"https://forms.gle/abcdefg\",\"isOpen\":false,"
                                + "\"closedMessage\":\"모집 예정\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/home"))
                .andExpect(jsonPath("$.recruit.isOpen").value(false))
                .andExpect(jsonPath("$.recruit.closedMessage").value("모집 예정"));
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
