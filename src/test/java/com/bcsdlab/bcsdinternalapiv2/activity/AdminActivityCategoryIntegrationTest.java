package com.bcsdlab.bcsdinternalapiv2.activity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityCategoryRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminActivityCategoryIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private ActivityCategoryRepository activityCategoryRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // @SQLRestriction 때문에 deleteAll()은 soft-delete된 행을 찾지 못해 물리적으로
        // 남겨 두고, 그 남은 행이 activity_category FK를 계속 참조해 다음 deleteAll()을 막는다.
        jdbcTemplate.update("delete from activity_image");
        jdbcTemplate.update("delete from activity");
        jdbcTemplate.update("delete from activity_category");
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();

        TrackMaster backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        Member admin = memberRepository.save(Member.builder()
                .studentNumber("20231111")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("관리자")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("admin@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build());
        String body = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentNumber\":\"20231111\",\"password\":\"%s\",\"rememberMe\":false}"
                                .formatted(RAW_PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        adminToken = objectMapper.readTree(body).get("accessToken").asText();
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);
    }

    @Test
    @DisplayName("카테고리를 생성하면 공개 목록에 나타난다")
    void 카테고리_생성과_공개_목록_노출() throws Exception {
        mockMvc.perform(post("/v1/admin/activity-categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"event\",\"name\":\"EVENT\",\"headline\":\"BCSD에서는\\n이런 활동을 하고 있어요.\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("event"));

        mockMvc.perform(get("/v1/activity-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("event"))
                .andExpect(jsonPath("$[0].headline").value("BCSD에서는\n이런 활동을 하고 있어요."));
    }

    @Test
    @DisplayName("slug 중복은 409다")
    void slug_중복은_409() throws Exception {
        activityCategoryRepository.save(ActivityCategory.builder()
                .slug("event").name("EVENT").displayOrder(0).published(true).build());

        mockMvc.perform(post("/v1/admin/activity-categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"event\",\"name\":\"EVENT2\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("숨긴 카테고리는 공개 목록에서 제외된다")
    void 숨긴_카테고리는_공개_목록에서_제외된다() throws Exception {
        ActivityCategory event = activityCategoryRepository.save(ActivityCategory.builder()
                .slug("event").name("EVENT").displayOrder(0).published(true).build());
        activityCategoryRepository.save(ActivityCategory.builder()
                .slug("game").name("GAME").displayOrder(1).published(true).build());

        mockMvc.perform(patch("/v1/admin/activity-categories/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPublished\":false}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/activity-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].slug").value("game"));
    }

    @Test
    @DisplayName("탭 순서를 바꾸면 공개 목록에 즉시 반영된다")
    void 순서_변경이_공개_목록에_반영된다() throws Exception {
        ActivityCategory event = activityCategoryRepository.save(ActivityCategory.builder()
                .slug("event").name("EVENT").displayOrder(0).published(true).build());
        ActivityCategory game = activityCategoryRepository.save(ActivityCategory.builder()
                .slug("game").name("GAME").displayOrder(1).published(true).build());

        mockMvc.perform(patch("/v1/admin/activity-categories/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(game.getId(), event.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/activity-categories"))
                .andExpect(jsonPath("$[0].slug").value("game"))
                .andExpect(jsonPath("$[1].slug").value("event"));
    }
}
