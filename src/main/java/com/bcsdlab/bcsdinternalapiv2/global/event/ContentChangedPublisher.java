package com.bcsdlab.bcsdinternalapiv2.global.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 태그 규칙(05-api-spec.md §4)을 한 곳에 묶어, 도메인 서비스가 태그 문자열을
 * 직접 조립하지 않게 한다.
 */
@Component
@RequiredArgsConstructor
public class ContentChangedPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void trackListChanged() {
        publish(List.of("track-list"));
    }

    public void trackChanged(String slug) {
        publish(List.of("track:" + slug));
    }

    public void trackAndListChanged(String slug) {
        publish(List.of("track-list", "track:" + slug));
    }

    public void activityCategoryChanged(String categorySlug) {
        publish(List.of("activity-category-list", "activity:" + categorySlug));
    }

    public void activityChanged(String categorySlug, Long activityId) {
        publish(List.of("activity:" + categorySlug, "activity:" + activityId));
    }

    public void homeChanged() {
        publish(List.of("home"));
    }

    public void publish(List<String> tags) {
        eventPublisher.publishEvent(new ContentChangedEvent(tags));
    }
}
