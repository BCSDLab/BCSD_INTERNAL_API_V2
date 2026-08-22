package com.bcsdlab.bcsdinternalapiv2.track;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TechStackRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminTrackIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private TrackPageRepository trackPageRepository;

    @Autowired
    private TechStackRepository techStackRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackMaster backend;
    private TrackMaster frontend;
    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUp() throws Exception {
        trackPageRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        frontend = trackMasterRepository.findByCode("FRONTEND").orElseThrow();

        Member admin = memberRepository.save(newMember("20231111", "admin@bcsd.club"));
        adminToken = login("20231111");
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);

        memberRepository.save(newMember("20232222", "member@bcsd.club"));
        memberToken = login("20232222");
    }

    @AfterEach
    void tearDown() {
        // track_page_member·refresh_token이 member를 참조하므로, 다음에 실행될 다른 테스트
        // 클래스의 memberRepository.deleteAll()이 FK 위반으로 막히지 않도록 여기서 먼저 정리한다.
        trackPageRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("AC-6.1 토큰 없이 호출하면 401이다")
    void 토큰_없이_호출하면_401() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackPageCreateBody(backend.getId(), "Backend")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AC-6.2 MEMBER 권한으로 호출하면 403이다")
    void member_권한으로_호출하면_403() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackPageCreateBody(backend.getId(), "Backend")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AC-1.1 표시명에서 slug를 자동 생성한다")
    void slug를_자동_생성한다() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackPageCreateBody(backend.getId(), "Data Analyst")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("data-analyst"))
                .andExpect(jsonPath("$.displayName").value("Data Analyst"));
    }

    @Test
    @DisplayName("한글 트랙명도 유효한 slug를 얻는다 — 두 개를 연달아 만들어도 충돌하지 않는다")
    void 한글_트랙명도_slug가_생긴다() throws Exception {
        // 파생 결과가 비면 트랙 코드로 떨어진다(BACKEND → backend).
        mockMvc.perform(post("/v1/admin/track-pages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackPageCreateBody(backend.getId(), "백엔드")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("backend"))
                .andExpect(jsonPath("$.displayName").value("백엔드"));

        // 예전에는 둘 다 빈 slug가 되어 uq_track_page_slug에 걸려 409였다.
        mockMvc.perform(post("/v1/admin/track-pages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackPageCreateBody(frontend.getId(), "프론트엔드")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("frontend"));

        // 공개 API도 그 slug로 찾을 수 있어야 한다(예전엔 빈 slug라 조회가 불가능했다).
        mockMvc.perform(get("/v1/tracks/backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("백엔드"));
    }

    @Test
    @DisplayName("AC-1.2 중복 slug는 409다")
    void 중복_slug는_409() throws Exception {
        trackPageRepository.save(trackPage(backend, "backend", 0));

        mockMvc.perform(post("/v1/admin/track-pages")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackPageCreateBody(frontend.getId(), "Backend")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("AC-1.4 순서 변경이 공개 목록에 즉시 반영된다")
    void 순서_변경이_공개_목록에_즉시_반영된다() throws Exception {
        TrackPage a = trackPageRepository.save(trackPage(backend, "backend", 0));
        TrackPage b = trackPageRepository.save(trackPage(frontend, "frontend", 1));

        mockMvc.perform(patch("/v1/admin/track-pages/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(b.getId(), a.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/tracks"))
                .andExpect(jsonPath("$[0].slug").value("frontend"))
                .andExpect(jsonPath("$[1].slug").value("backend"));
    }

    @Test
    @DisplayName("AC-1.5 id 집합이 다르면 400이고 아무것도 변경되지 않는다")
    void id_집합이_다르면_아무것도_바뀌지_않는다() throws Exception {
        TrackPage a = trackPageRepository.save(trackPage(backend, "backend", 0));
        trackPageRepository.save(trackPage(frontend, "frontend", 1));

        mockMvc.perform(patch("/v1/admin/track-pages/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d]}".formatted(a.getId())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/v1/tracks"))
                .andExpect(jsonPath("$[0].slug").value("backend"))
                .andExpect(jsonPath("$[1].slug").value("frontend"));
    }

    @Test
    @DisplayName("편집 화면 조회는 헤더와 함께 studyPoints·techStacks·members를 반환한다")
    void 편집_화면_조회는_studyPoints_techStacks_members를_포함한다() throws Exception {
        TrackPage trackPage = trackPageRepository.save(trackPage(backend, "backend", 0));

        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/study-points")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studyPoints\":[{\"title\":\"A\",\"description\":\"a\"}]}"))
                .andExpect(status().isOk());

        String techStackBody = mockMvc.perform(post("/v1/admin/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"TestStack\",\"iconUrl\":\"https://image.bcsdlab.com/test-stack.png\"}"))
                .andReturn().getResponse().getContentAsString();
        long techStackId = objectMapper.readTree(techStackBody).get("id").asLong();
        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"techStackIds\":[%d]}".formatted(techStackId)))
                .andExpect(status().isOk());

        Member candidate = memberRepository.save(newMember("20233333", "candidate@bcsd.club"));
        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d]}".formatted(candidate.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/admin/track-pages/" + trackPage.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyPoints[0].title").value("A"))
                .andExpect(jsonPath("$.techStacks[0].name").value("TestStack"))
                .andExpect(jsonPath("$.members[0].name").value(candidate.getName()));

        techStackRepository.deleteById(techStackId);
    }

    @Test
    @DisplayName("트랙 마스터 코드 중복 생성은 409, 이름·활성 여부 수정은 반영된다")
    void 트랙_마스터_생성과_수정() throws Exception {
        mockMvc.perform(post("/v1/admin/tracks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"BACKEND\",\"name\":\"백엔드\"}"))
                .andExpect(status().isConflict());

        String body = mockMvc.perform(post("/v1/admin/tracks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"BLOCKCHAIN\",\"name\":\"블록체인\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long newId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(put("/v1/admin/tracks/" + newId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"블록체인개발\",\"isActive\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("블록체인개발"))
                .andExpect(jsonPath("$.isActive").value(false));

        // track은 다른 테스트(TrackPromotionIntegrationTest)가 "시드된 11개뿐"이라고 가정하고
        // IntegrationTestSupport로 컨테이너를 공유한다 — 이 테스트가 만든 행은 직접 치운다.
        trackMasterRepository.deleteById(newId);
    }

    private Member newMember(String studentNumber, String email) {
        return Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("테스트")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email(email)
                .status(MemberStatus.ACTIVE)
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

    private String trackPageCreateBody(Long trackId, String displayName) {
        return "{\"trackId\":%d,\"displayName\":\"%s\",\"tagline\":\"tagline\"}".formatted(trackId, displayName);
    }

    private TrackPage trackPage(TrackMaster track, String slug, int order) {
        return TrackPage.builder()
                .track(track)
                .slug(slug)
                .displayName(slug)
                .tagline("tagline")
                .displayOrder(order)
                .published(true)
                .build();
    }
}
