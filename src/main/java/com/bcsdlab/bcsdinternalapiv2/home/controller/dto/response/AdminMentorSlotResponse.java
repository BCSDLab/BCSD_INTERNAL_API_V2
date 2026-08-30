package com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.home.model.MentorSlot;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;

public record AdminMentorSlotResponse(
        Long memberId,
        String name,
        String trackName,
        String profileImageUrl,
        int displayOrder
) {
    public static AdminMentorSlotResponse from(MentorSlot slot) {
        Member member = slot.getMember();
        return new AdminMentorSlotResponse(
                member.getId(), member.getName(), member.getTrack().getName(),
                member.getProfileImageUrl(), slot.getDisplayOrder());
    }
}
