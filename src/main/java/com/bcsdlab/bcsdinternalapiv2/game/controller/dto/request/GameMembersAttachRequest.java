package com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record GameMembersAttachRequest(
        @NotEmpty List<Long> memberIds
) {
}
