package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.NotificationFailureReason;
import java.util.List;

/**
 * 부분 실패도 200이다. 같은 요청 키로 다시 보내면 그 발송 건의 누적 결과다(sentCount는 이전에 보낸 회원 포함).
 * 나에게 테스트 발송이면 sentCount는 0 또는 1이고, 실패하면 failed에 요청한 관리자 id가 들어간다.
 */
public record DuesNotificationResponse(
        int sentCount,
        List<Failed> failed
) {

    public record Failed(Long memberId, NotificationFailureReason reason) {
    }
}
