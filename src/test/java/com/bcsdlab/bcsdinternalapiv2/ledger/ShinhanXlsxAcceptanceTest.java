package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer.Movement;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer.Sequenced;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.RowKeys;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ShinhanXlsxParser;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ShinhanXlsxParser.ParsedTransaction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * 실제 신한 거래내역으로 하는 로컬 수용 테스트(배포 조건, 설계 5-5절). 실제 파일은 개인정보라 레포에 넣지 않고,
 * LEDGER_IMPORT_ACCEPTANCE_DIR가 있을 때만 그 폴더의 .xlsx를 읽는다(없으면 건너뜀).
 *
 * <p>확인: 전부 파싱되는지, 파일 안과 파일 사이에서 은행 잔액이 이어지는지(앞 잔액 ± 금액 = 잔액), rowKey가 겹치지
 * 않는지. 결과는 건수만 출력하고, 실패 메시지에도 파일 순번·행 번호만 넣는다(이름·금액을 남기지 않는다).
 */
@EnabledIfEnvironmentVariable(named = "LEDGER_IMPORT_ACCEPTANCE_DIR", matches = ".+")
class ShinhanXlsxAcceptanceTest {

    @Test
    void 실제_거래내역_파일을_모두_파싱하고_잔액이_이어진다() throws IOException {
        Path dir = Path.of(System.getenv("LEDGER_IMPORT_ACCEPTANCE_DIR"));
        List<Path> files;
        try (Stream<Path> listing = Files.list(dir)) {
            files = listing.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".xlsx")).toList();
        }
        assertThat(files).as("xlsx 파일").isNotEmpty();

        List<FileResult> results = new ArrayList<>();
        int parseFailures = 0;
        for (Path file : files) {
            try {
                List<Sequenced<ParsedTransaction>> sequenced = ImportSequencer.sequence(
                        ShinhanXlsxParser.parse(Files.readAllBytes(file)),
                        t -> new Movement(t.occurredAt(), t.type(), t.amount(), t.bankBalance()));
                results.add(new FileResult(sequenced.stream().map(Sequenced::item).toList()));
            } catch (RuntimeException e) {
                parseFailures++;
            }
        }
        results.sort(Comparator.comparing(result -> result.transactions().getFirst().occurredAt()));

        int totalRows = 0;
        int breaksInFile = 0;
        int breaksBetweenFiles = 0;
        Set<String> rowKeys = new HashSet<>();
        int duplicateRowKeys = 0;
        ParsedTransaction previousFileLast = null;
        List<Integer> rowCounts = new ArrayList<>();
        for (FileResult result : results) {
            List<ParsedTransaction> transactions = result.transactions();
            rowCounts.add(transactions.size());
            totalRows += transactions.size();
            for (int i = 0; i < transactions.size(); i++) {
                ParsedTransaction current = transactions.get(i);
                if (!rowKeys.add(RowKeys.of(current.occurredAt(), current.type(), current.amount(),
                        current.bankBalance(), current.counterparty()))) {
                    duplicateRowKeys++;
                }
                if (i > 0 && !continues(transactions.get(i - 1), current)) {
                    breaksInFile++;
                }
            }
            if (previousFileLast != null && !continues(previousFileLast, transactions.getFirst())) {
                breaksBetweenFiles++;
            }
            previousFileLast = transactions.getLast();
        }

        System.out.printf("[수용 테스트] 파일 %d개, 파싱 실패 %d, 총 거래 %d건, 파일별 거래 %s, "
                        + "파일 안 잔액 끊김 %d, 파일 사이 잔액 끊김 %d, rowKey 중복 %d%n",
                files.size(), parseFailures, totalRows, rowCounts, breaksInFile, breaksBetweenFiles, duplicateRowKeys);

        assertThat(parseFailures).as("파싱 실패 파일 수").isZero();
        assertThat(breaksInFile).as("파일 안 잔액 끊김").isZero();
        assertThat(breaksBetweenFiles).as("파일 사이 잔액 끊김").isZero();
        assertThat(duplicateRowKeys).as("rowKey 중복").isZero();
    }

    private static boolean continues(ParsedTransaction previous, ParsedTransaction current) {
        long signed = current.type() == EntryType.DEPOSIT ? current.amount() : -current.amount();
        return previous.bankBalance() + signed == current.bankBalance();
    }

    private record FileResult(List<ParsedTransaction> transactions) {
    }
}
