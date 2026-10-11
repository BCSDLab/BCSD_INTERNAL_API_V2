package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.ImportCommitRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ImportCommitResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ImportPreviewResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.ImportFileInvalidType;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.ImportCategory;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportReader.PreviewContext;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer.Movement;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer.Sequenced;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ShinhanXlsxParser.ParsedTransaction;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 신한 xlsx 가져오기(설계 5-5절). 파싱(CPU 작업)은 트랜잭션 밖에서 하고, DB 조회·쓰기는 {@link ImportReader}·
 * {@link ImportWriter}가 한다. 파일 판정은 확장자와 zip·xlsx 구조로만 하고, 업로드 파트의 Content-Type은 보지 않는다
 * (브라우저마다 xlsx MIME·octet-stream·빈 값으로 다르다).
 */
@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportReader importReader;
    private final ImportWriter importWriter;

    public ImportPreviewResponse preview(MultipartFile file) {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().strip();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw ImportFileInvalidType.of("확장자가 .xlsx가 아닙니다");
        }
        if (file.isEmpty()) {
            throw ImportFileInvalidType.of("빈 파일입니다");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw ImportFileInvalidType.of("파일을 읽을 수 없습니다");
        }

        List<Sequenced<ParsedTransaction>> parsed = ImportSequencer.sequence(ShinhanXlsxParser.parse(bytes),
                t -> new Movement(t.occurredAt(), t.type(), t.amount(), t.bankBalance()));
        List<String> rowKeys = parsed.stream().map(s -> rowKeyOf(s.item())).toList();
        PreviewContext context = importReader.load(rowKeys);

        List<ImportPreviewResponse.Transaction> transactions = new java.util.ArrayList<>();
        for (int i = 0; i < parsed.size(); i++) {
            String rowKey = rowKeys.get(i);
            if (context.existingRowKeys().contains(rowKey)) {
                continue;
            }
            ParsedTransaction t = parsed.get(i).item();
            Long memberId = context.memberIdByName().get(t.counterparty());
            transactions.add(new ImportPreviewResponse.Transaction(rowKey, t.occurredAt().format(RowKeys.OCCURRED_AT),
                    t.type(), t.counterparty(), t.amount(), t.bankBalance(),
                    memberId == null ? ImportCategory.ETC : ImportCategory.DUES, memberId));
        }
        return new ImportPreviewResponse(fileName, transactions);
    }

    /**
     * 요청 안에 같은 거래(rowKey)가 두 번 오면 순서·쓰기 전에 거절한다. 그대로 두면 두 건이 같은 초 묶음에 들어가
     * 잔액 순서 복원을 흐트러뜨리고, 두 번째 건이 ON CONFLICT로 빠지면서 occurred_seq에 빈칸이 생기고
     * "이미 있던 거래"로 잘못 세어진다. 미리보기는 같은 rowKey를 두 번 내지 않는다(rowKey에 은행 잔액이 들어간다).
     */
    public ImportCommitResponse commit(ImportCommitRequest request, Long adminId) {
        Set<String> rowKeys = new HashSet<>();
        for (ImportCommitRequest.Transaction transaction : request.transactions()) {
            if (!rowKeys.add(transaction.rowKey())) {
                throw new LedgerException(LedgerExceptionType.DUPLICATED_ROW_KEY_IN_REQUEST);
            }
            EvidenceLinkWriter.requireAttachable(transaction.evidenceLinks(), List.of());
        }
        List<ImportWriter.Row> rows = request.transactions().stream().map(ImportService::verify).toList();
        List<Sequenced<ImportWriter.Row>> sequenced = ImportSequencer.sequence(rows, row -> new Movement(
                row.occurredAt(), row.source().type(), row.source().amount(), row.source().bankBalance()));
        return importWriter.commit(request.fileName().strip(), sequenced, adminId);
    }

    /** rowKey를 다시 계산해 대조한다. 실수(필드 누락·변형)를 막는 장치이고, 원본 파일과 같다는 증명은 아니다. */
    private static ImportWriter.Row verify(ImportCommitRequest.Transaction transaction) {
        LocalDateTime occurredAt;
        try {
            occurredAt = LocalDateTime.parse(transaction.occurredAt(), RowKeys.OCCURRED_AT);
        } catch (DateTimeParseException e) {
            throw new LedgerException(LedgerExceptionType.IMPORT_ROW_KEY_MISMATCH);
        }
        String counterparty = ShinhanXlsxParser.normalizeCounterparty(transaction.counterparty());
        String expected = RowKeys.of(occurredAt, transaction.type(), transaction.amount(), transaction.bankBalance(),
                counterparty);
        if (!expected.equals(transaction.rowKey())) {
            throw new LedgerException(LedgerExceptionType.IMPORT_ROW_KEY_MISMATCH);
        }
        return new ImportWriter.Row(transaction, occurredAt, counterparty);
    }

    private static String rowKeyOf(ParsedTransaction t) {
        return RowKeys.of(t.occurredAt(), t.type(), t.amount(), t.bankBalance(), t.counterparty());
    }
}
