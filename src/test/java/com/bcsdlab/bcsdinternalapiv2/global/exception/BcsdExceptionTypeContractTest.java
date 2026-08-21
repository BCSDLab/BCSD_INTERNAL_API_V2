package com.bcsdlab.bcsdinternalapiv2.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcsdlab.bcsdinternalapiv2.activity.exception.ActivityExceptionType;
import com.bcsdlab.bcsdinternalapiv2.curriculum.exception.CurriculumExceptionType;
import com.bcsdlab.bcsdinternalapiv2.media.exception.MediaExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * 도메인 예외 타입(Track/Curriculum/Activity/Media)은 아직 호출하는 컨트롤러가 없다(T-01은
 * 골격만 정의한다). 그래도 {@link BcsdExceptionType} 계약 — 상태 코드·메시지·{@code withDetail}
 * 조합이 실제로 동작하는지는 미리 검증해 둔다.
 */
class BcsdExceptionTypeContractTest {

    @Test
    @DisplayName("GlobalExceptionType은 지정한 상태·메시지를 그대로 반환한다")
    void globalExceptionType_기본_동작() {
        BcsdExceptionType type = GlobalExceptionType.ORDER_IDS_MISMATCH;

        assertThat(type.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(type.getMessage()).isEqualTo("순서 변경 대상이 기존 항목과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("withDetail은 원본 메시지와 상태를 유지한 채 상세 메시지를 덧붙인다")
    void withDetail은_상태를_바꾸지_않고_메시지에_상세를_덧붙인다() {
        BcsdExceptionType detailed = GlobalExceptionType.ORDER_IDS_MISMATCH.withDetail("weekId=42");

        assertThat(detailed.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(detailed.getMessage())
                .contains("순서 변경 대상이 기존 항목과 일치하지 않습니다.")
                .contains("weekId=42");
    }

    @Test
    @DisplayName("TrackExceptionType.TRACK_NOT_FOUND는 404다")
    void trackExceptionType() {
        assertThat(TrackExceptionType.TRACK_NOT_FOUND.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("CurriculumExceptionType.CURRICULUM_NOT_FOUND는 404다")
    void curriculumExceptionType() {
        assertThat(CurriculumExceptionType.CURRICULUM_NOT_FOUND.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("ActivityExceptionType.ACTIVITY_NOT_FOUND는 404다")
    void activityExceptionType() {
        assertThat(ActivityExceptionType.ACTIVITY_NOT_FOUND.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("MediaExceptionType.IMAGE_NOT_FOUND는 404다")
    void mediaExceptionType() {
        assertThat(MediaExceptionType.IMAGE_NOT_FOUND.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
