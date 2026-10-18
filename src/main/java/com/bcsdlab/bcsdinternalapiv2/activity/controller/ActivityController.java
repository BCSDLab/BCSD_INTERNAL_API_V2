package com.bcsdlab.bcsdinternalapiv2.activity.controller;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityTimelineResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.service.ActivityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/activities")
@RequiredArgsConstructor
public class ActivityController implements ActivityApi {

    private final ActivityService activityService;

    @Override
    @GetMapping
    public ResponseEntity<List<ActivityTimelineResponse>> getTimeline(@RequestParam String category) {
        return ResponseEntity.ok(activityService.getTimeline(category));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ActivityDetailResponse> getActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }
}
