package com.bcsdlab.bcsdinternalapiv2.homepage.service;

import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityCategoryRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedPublisher;
import com.bcsdlab.bcsdinternalapiv2.homepage.controller.dto.response.HomepageSyncResponse;
import com.bcsdlab.bcsdinternalapiv2.homepage.model.HomepageSyncStatus;
import com.bcsdlab.bcsdinternalapiv2.homepage.repository.HomepageSyncStatusRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomepageSyncService {

    private static final long STATUS_ID = 1L;

    private final HomepageSyncStatusRepository statusRepository;
    private final TrackPageRepository trackPageRepository;
    private final ActivityCategoryRepository activityCategoryRepository;
    private final GameRepository gameRepository;
    private final ContentChangedPublisher publisher;

    public HomepageSyncResponse getStatus() {
        return HomepageSyncResponse.from(statusRepository.findById(STATUS_ID).orElseThrow());
    }

    /** 운영진용 수동 복구 버튼 — 알려진 모든 태그를 강제로 무효화한다. */
    public void forceResync() {
        List<String> tags = new ArrayList<>();
        tags.add("track-list");
        trackPageRepository.findAll().forEach(trackPage -> tags.add("track:" + trackPage.getSlug()));
        tags.add("activity-category-list");
        activityCategoryRepository.findAllByOrderByDisplayOrderAsc()
                .forEach(category -> tags.add("activity:" + category.getSlug()));
        tags.add("game-list");
        gameRepository.findAllByOrderByDisplayOrderAsc().forEach(game -> tags.add("game:" + game.getSlug()));
        publisher.publish(tags);
    }
}
