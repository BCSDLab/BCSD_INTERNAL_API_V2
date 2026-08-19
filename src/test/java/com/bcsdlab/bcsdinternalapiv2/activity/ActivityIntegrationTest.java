package com.bcsdlab.bcsdinternalapiv2.activity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityImage;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityCategoryRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityImageRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class ActivityIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ActivityCategoryRepository activityCategoryRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityImageRepository activityImageRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ActivityCategory category;

    @BeforeEach
    void setUp() {
        // @SQLRestriction 때문에 deleteAll()은 soft-delete된 행을 찾지 못해 물리적으로
        // 남겨 두고, 그 남은 행이 activity_category FK를 계속 참조해 다음 deleteAll()을 막는다.
        jdbcTemplate.update("delete from activity_image");
        jdbcTemplate.update("delete from activity");
        jdbcTemplate.update("delete from activity_category");

        category = activityCategoryRepository.save(ActivityCategory.builder()
                .slug("event").name("EVENT").displayOrder(0).published(true).build());
    }

    @Test
    @DisplayName("AC-3.1 연도 내림차순, AC-3.2 같은 달 안에서는 display_order 순서다")
    void 연도_내림차순_같은_달은_순서대로() throws Exception {
        activityRepository.save(Activity.builder()
                .category(category).year(2019).month(1).title("A").summary("s").displayOrder(0).published(true)
                .build());
        Activity may2 = activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("5월 두번째").summary("s").displayOrder(1)
                .published(true).build());
        Activity may1 = activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("5월 첫번째").summary("s").displayOrder(0)
                .published(true).build());
        activityRepository.save(Activity.builder()
                .category(category).year(2020).month(3).title("B").summary("s").displayOrder(0).published(true)
                .build());

        mockMvc.perform(get("/v1/activities").queryParam("category", category.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2020))
                .andExpect(jsonPath("$[1].year").value(2019))
                .andExpect(jsonPath("$[1].activities[0].title").value(may1.getTitle()))
                .andExpect(jsonPath("$[1].activities[1].title").value(may2.getTitle()))
                .andExpect(jsonPath("$[1].activities[2].title").value("A"));
    }

    @Test
    @DisplayName("AC-3.4 목록의 thumbnailUrl은 사진 첫 장이다")
    void 썸네일은_첫_사진() throws Exception {
        Activity activity = activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("A").summary("s").displayOrder(0).published(true)
                .build());
        activityImageRepository.save(ActivityImage.builder()
                .activity(activity).imageUrl("https://x/1.png").displayOrder(0).build());
        activityImageRepository.save(ActivityImage.builder()
                .activity(activity).imageUrl("https://x/2.png").displayOrder(1).build());

        mockMvc.perform(get("/v1/activities").queryParam("category", category.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activities[0].thumbnailUrl").value("https://x/1.png"))
                .andExpect(jsonPath("$[0].activities[0].images.length()").value(2));
    }

    @Test
    @DisplayName("AC-3.9 content가 없으면 hasDetail은 false다")
    void 본문_없으면_hasDetail_false() throws Exception {
        activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("A").summary("s").displayOrder(0).published(true)
                .build());

        mockMvc.perform(get("/v1/activities").queryParam("category", category.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activities[0].hasDetail").value(false));
    }

    @Test
    @DisplayName("AC-3.7 숨긴 활동은 목록에서 사라지고 상세는 404다")
    void 숨긴_활동은_목록_제외_상세_404() throws Exception {
        Activity hidden = activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("숨김").summary("s").displayOrder(0).published(false)
                .build());
        activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("공개").summary("s").displayOrder(1).published(true)
                .build());

        mockMvc.perform(get("/v1/activities").queryParam("category", category.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activities.length()").value(1))
                .andExpect(jsonPath("$[0].activities[0].title").value("공개"));

        mockMvc.perform(get("/v1/activities/" + hidden.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("활동 상세는 본문과 카테고리 slug를 포함한다")
    void 상세_응답() throws Exception {
        Activity activity = activityRepository.save(Activity.builder()
                .category(category).year(2019).month(5).title("A").summary("s").content("<p>본문</p>")
                .displayOrder(0).published(true).build());

        mockMvc.perform(get("/v1/activities/" + activity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorySlug").value(category.getSlug()))
                .andExpect(jsonPath("$.content").value("<p>본문</p>"));
    }

    @Test
    @DisplayName("존재하지 않거나 숨겨진 카테고리로 조회하면 404다")
    void 존재하지_않는_카테고리는_404() throws Exception {
        mockMvc.perform(get("/v1/activities").queryParam("category", "no-such-category"))
                .andExpect(status().isNotFound());
    }
}
