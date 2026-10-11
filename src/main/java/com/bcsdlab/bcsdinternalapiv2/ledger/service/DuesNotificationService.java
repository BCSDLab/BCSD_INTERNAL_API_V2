package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesNotificationRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.DuesNotificationResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.NotificationFailureReason;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesNotificationRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesNotificationRepository.RecipientRow;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesNotificationReader.Context;
import com.bcsdlab.bcsdinternalapiv2.global.config.SlackProperties;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackApiException;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackPostResult;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 회비 Slack 알림 발송. 봇이 회원마다 DM(chat.postMessage, channel = 회원 Slack ID)을 보낸다.
 *
 * <ul>
 *   <li>Slack 호출은 트랜잭션 밖에서 하고, 결과는 회원마다 {@link DuesNotificationWriter}가 바로 커밋한다.</li>
 *   <li>같은 Idempotency-Key로 다시 요청하면 이미 보낸 회원은 건너뛴다.</li>
 *   <li>FE 요청 제한(30초)보다 짧게 20초가 지나면 새 발송을 시작하지 않는다. 못 보낸 회원은 SLACK_ERROR이고 같은 키로
 *       다시 요청하면 그 회원만 보낸다.</li>
 *   <li>429는 Retry-After가 남은 시간 안이면 한 번만 기다렸다가 다시 보낸다.</li>
 *   <li>로그에는 회원 id와 Slack 오류 코드만 남긴다. 본문과 토큰은 남기지 않는다.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DuesNotificationService {

    static final Duration SEND_BUDGET = Duration.ofSeconds(20);
    private static final Pattern IDEMPOTENCY_KEY = Pattern.compile("^[A-Za-z0-9-]{8,64}$");
    private static final Set<String> AUTH_ERRORS = Set.of("missing_scope", "not_authed", "invalid_auth",
            "account_inactive", "token_revoked");

    private final Clock clock;
    private final DuesNotificationReader reader;
    private final DuesNotificationWriter writer;
    private final DuesNotificationRepository repository;
    private final SlackClient slackClient;
    private final SlackProperties slackProperties;

    public DuesNotificationResponse send(String semesterId, DuesNotificationRequest request, String idempotencyKey,
                                         Long adminId) {
        Instant deadline = Instant.now(clock).plus(SEND_BUDGET);
        List<DuesNotificationRequest.Message> messages = request.messages();
        Set<Long> distinct = new HashSet<>();
        for (DuesNotificationRequest.Message message : messages) {
            if (!distinct.add(message.memberId())) {
                throw new LedgerException(LedgerExceptionType.DUPLICATED_MEMBER_IN_REQUEST);
            }
        }
        Context context = reader.load(semesterId, distinct, adminId);
        boolean mentionUsed = messages.stream().anyMatch(m -> SlackMessageFormatter.containsManagerMention(m.message()));
        if (mentionUsed && context.adminSlackId() == null) {
            throw new LedgerException(LedgerExceptionType.SENDER_SLACK_ID_MISSING);
        }
        String requestHash = requestHash(context.semesterId(), request);

        if (request.isTestToSelf()) {
            return sendTestToSelf(request, context, requestHash, adminId, deadline);
        }
        if (idempotencyKey == null || !IDEMPOTENCY_KEY.matcher(idempotencyKey).matches()) {
            throw new LedgerException(LedgerExceptionType.IDEMPOTENCY_KEY_REQUIRED);
        }
        List<Long> memberIds = messages.stream().map(DuesNotificationRequest.Message::memberId).toList();
        DuesNotificationWriter.Started started = writer.start(idempotencyKey, requestHash, context.semesterId(),
                request.type().name(), adminId, memberIds);

        for (DuesNotificationRequest.Message message : messages) {
            if (started.alreadySent().contains(message.memberId())) {
                continue;
            }
            deliver(started.batchId(), message.memberId(), context.slackIds().get(message.memberId()),
                    message.message(), context.adminSlackId(), deadline);
        }
        writer.finish(started.batchId());
        return response(started.batchId(), memberIds);
    }

    /** 첫 번째 본문만 렌더링해 요청한 관리자 본인에게 1건 보낸다. 명단 검사는 메시지의 회원 기준으로 이미 했다. */
    private DuesNotificationResponse sendTestToSelf(DuesNotificationRequest request, Context context,
                                                    String requestHash, Long adminId, Instant deadline) {
        if (context.adminSlackId() == null) {
            throw new LedgerException(LedgerExceptionType.TEST_SENDER_SLACK_ID_MISSING);
        }
        long batchId = writer.startTest(requestHash, context.semesterId(), request.type().name(), adminId);
        deliver(batchId, adminId, context.adminSlackId(), request.messages().getFirst().message(),
                context.adminSlackId(), deadline);
        writer.finish(batchId);
        return response(batchId, List.of(adminId));
    }

    private void deliver(long batchId, Long memberId, String slackId, String body, String adminSlackId,
                         Instant deadline) {
        if (slackId == null || slackId.isBlank()) {
            writer.markFailed(batchId, memberId, NotificationFailureReason.SLACK_ID_MISSING);
            return;
        }
        if (slackProperties.botToken() == null || slackProperties.botToken().isBlank()) {
            log.warn("DUES_NOTIFICATION_NOT_SENT memberId={} — 서버에 SLACK_BOT_TOKEN이 없다", memberId);
            writer.markFailed(batchId, memberId, NotificationFailureReason.SLACK_ERROR);
            return;
        }
        if (Instant.now(clock).isAfter(deadline)) {
            log.warn("DUES_NOTIFICATION_NOT_SENT memberId={} — 시간 예산 {}초 초과, 같은 키로 다시 요청하면 보낸다",
                    memberId, SEND_BUDGET.toSeconds());
            writer.markFailed(batchId, memberId, NotificationFailureReason.SLACK_ERROR);
            return;
        }
        String text = SlackMessageFormatter.format(body, adminSlackId);
        try {
            SlackPostResult result = postWithOneRetry(slackId, text, deadline);
            writer.markSent(batchId, memberId, result.channel(), result.ts());
        } catch (SlackApiException e) {
            if (AUTH_ERRORS.contains(e.getError())) {
                log.error("DUES_NOTIFICATION_FAILED memberId={} slackError={} — 봇 scope chat:write와 SLACK_BOT_TOKEN을 "
                        + "확인하세요", memberId, e.getError());
            } else {
                log.warn("DUES_NOTIFICATION_FAILED memberId={} slackError={}", memberId, e.getError());
            }
            writer.markFailed(batchId, memberId, NotificationFailureReason.SLACK_ERROR);
        } catch (RuntimeException e) {
            log.warn("DUES_NOTIFICATION_FAILED memberId={} exception={}", memberId, e.getClass().getSimpleName());
            writer.markFailed(batchId, memberId, NotificationFailureReason.SLACK_ERROR);
        }
    }

    private SlackPostResult postWithOneRetry(String slackId, String text, Instant deadline) {
        try {
            return slackClient.postDirectMessage(slackId, text);
        } catch (SlackApiException e) {
            if (!e.isRateLimited()
                    || Instant.now(clock).plusSeconds(e.getRetryAfterSeconds()).isAfter(deadline)) {
                throw e;
            }
            sleep(e.getRetryAfterSeconds());
            return slackClient.postDirectMessage(slackId, text);
        }
    }

    private static void sleep(long seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SlackApiException("interrupted", null);
        }
    }

    /** 그 발송 건의 누적 결과. failed는 요청 순서대로다. */
    private DuesNotificationResponse response(long batchId, List<Long> order) {
        Map<Long, RecipientRow> rows = repository.findRecipients(batchId).stream()
                .collect(Collectors.toMap(RecipientRow::memberId, Function.identity()));
        int sent = (int) rows.values().stream().filter(row -> "SENT".equals(row.status())).count();
        List<DuesNotificationResponse.Failed> failed = new ArrayList<>();
        for (Long memberId : order) {
            RecipientRow row = rows.get(memberId);
            if (row != null && "FAILED".equals(row.status())) {
                failed.add(new DuesNotificationResponse.Failed(memberId, row.failureReason()));
            }
        }
        return new DuesNotificationResponse(sent, failed);
    }

    /**
     * 같은 키로 다른 내용을 보내는지 판별한다: 학기, type, (회원 id, 본문 해시)를 회원 id 순으로. 학기가 빠지면 같은 키·같은
     * 본문을 다른 학기로 보낼 때 이전 학기 발송의 재시도로 보고 아무것도 보내지 않는다. 본문 자체는 저장하지 않는다.
     * testToSelf는 넣지 않는다 — 테스트 발송은 요청 키를 쓰지 않아 이 해시로 기존 발송 건과 대조하는 일이 없다.
     */
    static String requestHash(Long semesterId, DuesNotificationRequest request) {
        String source = semesterId + "\n" + request.type().name() + "\n" + request.messages().stream()
                .sorted(Comparator.comparing(DuesNotificationRequest.Message::memberId))
                .map(m -> m.memberId() + ":" + sha256(m.message()))
                .collect(Collectors.joining("\n"));
        return sha256(source);
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
