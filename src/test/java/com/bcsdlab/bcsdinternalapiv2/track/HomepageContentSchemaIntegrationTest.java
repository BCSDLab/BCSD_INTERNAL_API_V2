package com.bcsdlab.bcsdinternalapiv2.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * T-05(V9__create_homepage_content.sql)의 제약 조건이 설계대로 동작하는지 검증한다.
 * 아직 JPA 엔티티가 없으므로(T-06 이후 도메인 티켓의 몫) raw SQL로 직접 검증한다 —
 * partial unique index, CHECK 제약, ON DELETE CASCADE처럼 오타 하나로도 조용히 깨지는
 * DDL을 엔티티가 생기기 전에 먼저 굳혀 둔다.
 */
class HomepageContentSchemaIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long frontendTrackId;
    private Long backendTrackId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from activity");
        jdbcTemplate.update("delete from activity_category");
        jdbcTemplate.update("delete from curriculum");
        jdbcTemplate.update("delete from track_page");
        jdbcTemplate.update("delete from image_asset");

        frontendTrackId = jdbcTemplate.queryForObject(
                "select id from track where code = 'FRONTEND'", Long.class);
        backendTrackId = jdbcTemplate.queryForObject(
                "select id from track where code = 'BACKEND'", Long.class);
    }

    @Test
    @DisplayName("INV-1 slug는 삭제되지 않은 track_page끼리만 유일하다")
    void track_page_slug는_삭제되지_않은_행끼리만_유일하다() {
        insertTrackPage(frontendTrackId, "frontend");

        assertThatThrownBy(() -> insertTrackPage(backendTrackId, "frontend"))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbcTemplate.update("update track_page set deleted_at = now() where slug = 'frontend'");

        // 예외를 던지지 않으면 성공
        insertTrackPage(backendTrackId, "frontend");
    }

    @Test
    @DisplayName("INV-2 한 트랙에 공개 커리큘럼 세트는 하나만 허용한다")
    void 공개_커리큘럼은_트랙당_하나만_허용한다() {
        Long trackPageId = insertTrackPage(frontendTrackId, "frontend");
        insertCurriculum(trackPageId, "비기너", true);

        assertThatThrownBy(() -> insertCurriculum(trackPageId, "심화", true))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 공개가 아니면 여러 개 허용
        insertCurriculum(trackPageId, "초안", false);
    }

    @Test
    @DisplayName("INV-5 week_to가 week_from보다 작으면 거부된다")
    void 주차_범위가_거꾸로면_거부된다() {
        Long trackPageId = insertTrackPage(frontendTrackId, "frontend");
        Long curriculumId = insertCurriculum(trackPageId, "비기너", true);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into curriculum_week (curriculum_id, week_from, week_to, display_order) "
                        + "values (?, 5, 3, 0)", curriculumId))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbcTemplate.update(
                "insert into curriculum_week (curriculum_id, week_from, week_to, display_order) "
                        + "values (?, 13, 16, 0)", curriculumId);
        jdbcTemplate.update(
                "insert into curriculum_week (curriculum_id, week_from, week_to, display_order) "
                        + "values (?, 4, null, 1)", curriculumId);
    }

    @Test
    @DisplayName("INV-6 활동의 month/year 범위를 벗어나면 거부된다")
    void 활동_연월_범위를_벗어나면_거부된다() {
        Long categoryId = insertActivityCategory("event", "EVENT");

        assertThatThrownBy(() -> insertActivity(categoryId, 2019, 13))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertActivity(categoryId, 1999, 5))
                .isInstanceOf(DataIntegrityViolationException.class);

        insertActivity(categoryId, 2019, 5);
    }

    @Test
    @DisplayName("INV-7 커리큘럼을 삭제하면 주차·토픽·세부항목이 cascade로 함께 삭제된다")
    void 커리큘럼_삭제는_하위_트리에_cascade된다() {
        Long trackPageId = insertTrackPage(frontendTrackId, "frontend");
        Long curriculumId = insertCurriculum(trackPageId, "비기너", true);
        Long weekId = jdbcTemplate.queryForObject(
                "insert into curriculum_week (curriculum_id, week_from, display_order) "
                        + "values (?, 1, 0) returning id",
                Long.class, curriculumId);
        Long topicId = jdbcTemplate.queryForObject(
                "insert into curriculum_topic (week_id, title, display_order) values (?, '웹 동작 방식', 0) "
                        + "returning id",
                Long.class, weekId);
        jdbcTemplate.update(
                "insert into curriculum_topic_detail (topic_id, content, display_order) values (?, 'HTTP', 0)",
                topicId);

        jdbcTemplate.update("delete from curriculum where id = ?", curriculumId);

        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from curriculum_week where id = ?", Integer.class, weekId)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from curriculum_topic where id = ?", Integer.class, topicId)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from curriculum_topic_detail where topic_id = ?", Integer.class, topicId))
                .isZero();
    }

    @Test
    @DisplayName("INV-7 활동을 삭제하면 사진도 cascade로 함께 삭제된다")
    void 활동_삭제는_사진에_cascade된다() {
        Long categoryId = insertActivityCategory("event", "EVENT");
        Long activityId = insertActivity(categoryId, 2019, 5);
        jdbcTemplate.update(
                "insert into activity_image (activity_id, image_url, display_order) values (?, 'https://x/1.png', 0)",
                activityId);

        jdbcTemplate.update("delete from activity where id = ?", activityId);

        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from activity_image where activity_id = ?", Integer.class, activityId))
                .isZero();
    }

    @Test
    @DisplayName("카테고리에 활동이 남아 있으면 FK 제약으로 삭제가 거부된다 (AC-3.8의 DB 쪽 근거)")
    void 활동이_남은_카테고리는_삭제할_수_없다() {
        Long categoryId = insertActivityCategory("event", "EVENT");
        insertActivity(categoryId, 2019, 5);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from activity_category where id = ?", categoryId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long insertTrackPage(Long trackId, String slug) {
        return jdbcTemplate.queryForObject(
                "insert into track_page (track_id, slug, display_name, tagline, display_order) "
                        + "values (?, ?, ?, 'tagline', 0) returning id",
                Long.class, trackId, slug, slug);
    }

    private Long insertCurriculum(Long trackPageId, String name, boolean published) {
        return jdbcTemplate.queryForObject(
                "insert into curriculum (track_page_id, name, is_published, display_order) "
                        + "values (?, ?, ?, 0) returning id",
                Long.class, trackPageId, name, published);
    }

    private Long insertActivityCategory(String slug, String name) {
        return jdbcTemplate.queryForObject(
                "insert into activity_category (slug, name, display_order) values (?, ?, 0) returning id",
                Long.class, slug, name);
    }

    private Long insertActivity(Long categoryId, int year, int month) {
        return jdbcTemplate.queryForObject(
                "insert into activity (category_id, year, month, title, summary, display_order) "
                        + "values (?, ?, ?, 't', 's', 0) returning id",
                Long.class, categoryId, year, month);
    }
}
