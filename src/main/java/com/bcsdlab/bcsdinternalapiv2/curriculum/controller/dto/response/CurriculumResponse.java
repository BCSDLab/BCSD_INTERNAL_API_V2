package com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response;

import java.util.List;

/**
 * 공개 트랙 상세(GET /v1/tracks/{slug})에 담기는 커리큘럼. 공개 세트가 없으면
 * null이다(AC-2.2). 토픽이 0개인 주차는 weeks에서 제외한다(AC-2.7).
 */
public record CurriculumResponse(
        String name,
        List<WeekResponse> weeks
) {
    public record WeekResponse(
            Integer weekFrom,
            Integer weekTo,
            List<TopicResponse> topics
    ) {
    }

    public record TopicResponse(
            String title,
            List<String> details
    ) {
    }
}
