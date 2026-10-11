package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesNotificationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 회비 Slack 알림. 본문은 FE가 회원마다 만들고 {담당자 멘션}은 그대로 보낸다(서버가 로그인 관리자의 멘션으로 바꾼다).
 * testToSelf가 true면 첫 번째 본문만 요청한 관리자 본인에게 1건 보낸다(운영 첫 확인용).
 */
public record DuesNotificationRequest(
        @NotNull(message = "알림 종류를 선택하세요.")
        DuesNotificationType type,

        @NotEmpty(message = "보낼 회원을 선택하세요.")
        @Size(max = 50, message = "한 번에 50명까지 보낼 수 있습니다.")
        List<@Valid @NotNull Message> messages,

        Boolean testToSelf
) {

    public boolean isTestToSelf() {
        return Boolean.TRUE.equals(testToSelf);
    }

    public record Message(
            @NotNull(message = "회원을 선택하세요.")
            Long memberId,

            @NotBlank(message = "메시지를 입력하세요.")
            @Size(max = 4000, message = "메시지는 4,000자 이하여야 합니다.")
            String message
    ) {
    }
}
