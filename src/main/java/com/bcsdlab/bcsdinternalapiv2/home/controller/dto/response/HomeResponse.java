package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response;

import java.util.List;

public record HomeResponse(
        List<MentorResponse> mentors,
        List<QnaResponse> qna,
        RecruitLinkResponse recruit
) {
}
