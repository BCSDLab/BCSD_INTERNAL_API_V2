package com.bcsdlab.bcsdinternalapiv2.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityCategoryRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityImageRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityRepository;
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

class AdminActivityIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private ActivityCategoryRepository activityCategoryRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityImageRepository activityImageRepository;

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

    private ActivityCategory category;
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

        category = activityCategoryRepository.save(ActivityCategory.builder()
                .slug("event").name("EVENT").displayOrder(0).published(true).build());

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
    @DisplayName("활동 생성 시 본문의 script 태그가 저장 전에 제거된다 (AC-3.5)")
    void 생성_시_본문이_정제된다() throws Exception {
        String body = mockMvc.perform(post("/v1/admin/activities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("{\"categoryId\":%d,\"year\":2019,\"month\":5,\"title\":\"컨퍼런스\","
                                + "\"summary\":\"요약\",\"content\":\"<p>안내</p><script>alert(1)</script>\"}")
                                .formatted(category.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String content = objectMapper.readTree(body).get("content").asText();
        assertThat(content).doesNotContainIgnoringCase("<script").contains("안내");
    }

    @Test
    @DisplayName("AC-3.3 month가 범위를 벗어나면 400이다")
    void month_범위를_벗어나면_400() throws Exception {
        mockMvc.perform(post("/v1/admin/activities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":%d,\"year\":2019,\"month\":13,\"title\":\"t\",\"summary\":\"s\"}"
                                .formatted(category.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AC-3.4 사진을 여러 장 올리면 첫 장이 썸네일 위치(순서 0)다")
    void 사진_전체_교체와_썸네일_순서() throws Exception {
        long activityId = createActivity();

        mockMvc.perform(put("/v1/admin/activities/" + activityId + "/images")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrls\":[\"https://x/1.png\",\"https://x/2.png\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("https://x/1.png"));

        var images = activityImageRepository.findAllByActivity_IdOrderByDisplayOrderAsc(activityId);
        assertThat(images).hasSize(2);
        assertThat(images.get(0).getDisplayOrder()).isZero();
        assertThat(images.get(0).getImageUrl()).isEqualTo("https://x/1.png");
    }

    @Test
    @DisplayName("같은 (카테고리, 연, 월) 안에서 순서를 바꿀 수 있다")
    void 같은_연월_안에서_순서_변경() throws Exception {
        Activity a = activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("A").summary("s").displayOrder(0).published(true)
                .build());
        Activity b = activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("B").summary("s").displayOrder(1).published(true)
                .build());

        mockMvc.perform(patch("/v1/admin/activities/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .queryParam("categoryId", category.getId().toString())
                        .queryParam("year", "2019")
                        .queryParam("month", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(b.getId(), a.getId())))
                .andExpect(status().isNoContent());

        assertThat(activityRepository.findById(b.getId()).orElseThrow().getDisplayOrder()).isZero();
        assertThat(activityRepository.findById(a.getId()).orElseThrow().getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("AC-3.8 활동이 남아 있는 카테고리는 삭제할 수 없다")
    void 활동이_남은_카테고리는_삭제할_수_없다() throws Exception {
        activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("A").summary("s").displayOrder(0).published(true)
                .build());

        mockMvc.perform(delete("/v1/admin/activity-categories/" + category.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/v1/activity-categories"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("활동 공개/숨김과 soft delete가 반영된다")
    void 공개_숨김과_삭제() throws Exception {
        long activityId = createActivity();

        mockMvc.perform(patch("/v1/admin/activities/" + activityId + "/publish")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPublished\":false}"))
                .andExpect(status().isNoContent());
        assertThat(activityRepository.findById(activityId).orElseThrow().isPublished()).isFalse();

        mockMvc.perform(delete("/v1/admin/activities/" + activityId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        assertThat(activityRepository.findById(activityId)).isEmpty();
    }

    private long createActivity() throws Exception {
        String body = mockMvc.perform(post("/v1/admin/activities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":%d,\"year\":2019,\"month\":5,\"title\":\"t\",\"summary\":\"s\"}"
                                .formatted(category.getId())))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
