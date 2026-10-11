package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.ImportFileInvalidType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * 신한은행 거래내역 xlsx 파서(설계 5-5절). 라이브러리 없이 JDK zip + StAX만 쓴다.
 *
 * <ul>
 *   <li>읽는 엔트리: workbook.xml → workbook.xml.rels로 첫 시트 경로, sharedStrings.xml(있으면), 첫 시트</li>
 *   <li>헤더: 처음 10행 안에서 거래일시·입금액·출금액·내용·잔액을 모두 가진 행. 열 위치는 헤더 이름으로 정한다</li>
 *   <li>문자열은 sharedStrings(t="s", 엑셀 재저장본)와 inlineStr(은행 원본) 둘 다 읽는다</li>
 *   <li>금액은 숫자 셀("NNNNN.0")과 쉼표 문자열("N,NNN") 둘 다 정수로 읽는다</li>
 *   <li>빈 행과 거래일시가 "합계"인 행은 건너뛴다</li>
 * </ul>
 * 오류 메시지에는 행 번호와 열 이름만 넣는다. 셀 값(실명·금액)은 메시지나 로그에 남기지 않는다.
 */
public final class ShinhanXlsxParser {

    static final int MAX_SHEET_ROWS = 20_000;
    static final int MAX_TRANSACTIONS = 5_000;
    static final int MAX_CELL_CHARS = 1_000;
    static final int MAX_COUNTERPARTY_CHARS = 100;
    private static final int HEADER_SEARCH_ROWS = 10;
    private static final String TOTAL_ROW = "합계";
    private static final String EMPTY_COUNTERPARTY = "(내용 없음)";
    private static final Pattern COMMA_INTEGER = Pattern.compile("^-?[0-9][0-9,]*$");
    private static final DateTimeFormatter OCCURRED_AT = DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

    private ShinhanXlsxParser() {
    }

    /** 파일 순서 그대로 거래를 돌려준다. 같은 초 순서는 {@link ImportSequencer}가 정한다. */
    public static List<ParsedTransaction> parse(byte[] file) {
        Map<String, byte[]> entries = XlsxZipGuard.unzip(file);
        String sheetPath = firstSheetPath(entries);
        byte[] sheet = entries.get(sheetPath);
        if (sheet == null) {
            throw ImportFileInvalidType.of("시트를 찾을 수 없습니다");
        }
        byte[] shared = entries.get("xl/sharedStrings.xml");
        List<String> sharedStrings = shared == null ? List.of() : readSharedStrings(shared);
        return readTransactions(readRows(sheet, sharedStrings));
    }

    // ---------- 시트 찾기 ----------

    private static String firstSheetPath(Map<String, byte[]> entries) {
        byte[] workbook = entries.get("xl/workbook.xml");
        byte[] rels = entries.get("xl/_rels/workbook.xml.rels");
        if (workbook == null || rels == null) {
            throw ImportFileInvalidType.of("xlsx 파일이 아닙니다");
        }
        String relationId = null;
        XMLStreamReader reader = open(workbook);
        try {
            while (reader.hasNext() && relationId == null) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT && "sheet".equals(reader.getLocalName())) {
                    relationId = reader.getAttributeValue(REL_NS, "id");
                }
            }
            if (relationId == null) {
                throw ImportFileInvalidType.of("시트를 찾을 수 없습니다");
            }
            reader = open(rels);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT
                        && "Relationship".equals(reader.getLocalName())
                        && relationId.equals(reader.getAttributeValue(null, "Id"))) {
                    String target = reader.getAttributeValue(null, "Target");
                    if (target == null) {
                        break;
                    }
                    return target.startsWith("/") ? target.substring(1) : "xl/" + target;
                }
            }
        } catch (XMLStreamException e) {
            throw ImportFileInvalidType.of("xlsx 구조를 읽을 수 없습니다");
        }
        throw ImportFileInvalidType.of("시트를 찾을 수 없습니다");
    }

    // ---------- 문자열·행 읽기 ----------

    private static List<String> readSharedStrings(byte[] xml) {
        List<String> strings = new ArrayList<>();
        XMLStreamReader reader = open(xml);
        try {
            StringBuilder current = null;
            int phoneticDepth = 0;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    switch (reader.getLocalName()) {
                        case "si" -> current = new StringBuilder();
                        case "rPh" -> phoneticDepth++;
                        case "t" -> {
                            if (current != null && phoneticDepth == 0) {
                                appendLimited(current, reader.getElementText());
                            }
                        }
                        default -> { }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("si".equals(reader.getLocalName()) && current != null) {
                        strings.add(current.toString());
                        current = null;
                    } else if ("rPh".equals(reader.getLocalName())) {
                        phoneticDepth--;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw ImportFileInvalidType.of("xlsx 구조를 읽을 수 없습니다");
        }
        return strings;
    }

    /** 시트의 행을 (열 번호 → 셀 문자열)로 읽는다. 숫자 셀은 원문 그대로("12345.0") 둔다. */
    private static List<Row> readRows(byte[] xml, List<String> sharedStrings) {
        List<Row> rows = new ArrayList<>();
        XMLStreamReader reader = open(xml);
        try {
            Row row = null;
            int nextColumn = 0;
            String cellType = null;
            int cellColumn = -1;
            StringBuilder cellText = null;
            boolean inValue = false;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    switch (reader.getLocalName()) {
                        case "row" -> {
                            if (rows.size() >= MAX_SHEET_ROWS) {
                                throw ImportFileInvalidType.of("시트의 행이 너무 많습니다");
                            }
                            String r = reader.getAttributeValue(null, "r");
                            row = new Row(r == null ? rows.size() + 1 : Integer.parseInt(r));
                            nextColumn = 0;
                        }
                        case "c" -> {
                            String ref = reader.getAttributeValue(null, "r");
                            cellColumn = ref == null ? nextColumn : columnIndex(ref);
                            nextColumn = cellColumn + 1;
                            cellType = reader.getAttributeValue(null, "t");
                            cellText = new StringBuilder();
                        }
                        case "v", "t" -> inValue = cellText != null;
                        default -> { }
                    }
                } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                    if (inValue) {
                        appendLimited(cellText, reader.getText());
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    switch (reader.getLocalName()) {
                        case "v", "t" -> inValue = false;
                        case "c" -> {
                            if (row != null && cellText != null) {
                                row.cells.put(cellColumn, cellValue(cellType, cellText.toString(), sharedStrings));
                            }
                            cellText = null;
                        }
                        case "row" -> {
                            if (row != null) {
                                rows.add(row);
                            }
                            row = null;
                        }
                        default -> { }
                    }
                }
            }
        } catch (XMLStreamException | NumberFormatException e) {
            throw ImportFileInvalidType.of("시트를 읽을 수 없습니다");
        }
        return rows;
    }

    private static String cellValue(String type, String raw, List<String> sharedStrings) {
        if ("s".equals(type)) {
            try {
                int index = Integer.parseInt(raw.strip());
                if (index < 0 || index >= sharedStrings.size()) {
                    throw ImportFileInvalidType.of("문자열 표를 읽을 수 없습니다");
                }
                return sharedStrings.get(index);
            } catch (NumberFormatException e) {
                throw ImportFileInvalidType.of("문자열 표를 읽을 수 없습니다");
            }
        }
        return raw;
    }

    // ---------- 거래 만들기 ----------

    private static List<ParsedTransaction> readTransactions(List<Row> rows) {
        Map<ShinhanColumn, Integer> columns = null;
        int headerIndex = -1;
        for (int i = 0; i < Math.min(HEADER_SEARCH_ROWS, rows.size()) && columns == null; i++) {
            columns = headerColumns(rows.get(i));
            headerIndex = i;
        }
        if (columns == null) {
            throw ImportFileInvalidType.of("빠진 열: " + missingColumns(rows));
        }

        List<ParsedTransaction> transactions = new ArrayList<>();
        for (int i = headerIndex + 1; i < rows.size(); i++) {
            Row row = rows.get(i);
            String occurredAt = row.text(columns.get(ShinhanColumn.OCCURRED_AT));
            String deposit = row.text(columns.get(ShinhanColumn.DEPOSIT));
            String withdrawal = row.text(columns.get(ShinhanColumn.WITHDRAWAL));
            String content = row.text(columns.get(ShinhanColumn.CONTENT));
            String balance = row.text(columns.get(ShinhanColumn.BALANCE));
            if (occurredAt.isEmpty() && deposit.isEmpty() && withdrawal.isEmpty() && content.isEmpty()
                    && balance.isEmpty()) {
                continue;
            }
            if (TOTAL_ROW.equals(occurredAt)) {
                continue;
            }
            if (transactions.size() >= MAX_TRANSACTIONS) {
                throw ImportFileInvalidType.of("거래가 5,000건을 넘습니다");
            }
            transactions.add(toTransaction(row.number, occurredAt, deposit, withdrawal, content, balance));
        }
        return transactions;
    }

    private static ParsedTransaction toTransaction(int rowNumber, String occurredAtText, String depositText,
                                                   String withdrawalText, String content, String balanceText) {
        LocalDateTime occurredAt;
        try {
            occurredAt = LocalDateTime.parse(occurredAtText, OCCURRED_AT);
        } catch (DateTimeParseException e) {
            throw ImportFileInvalidType.of(rowNumber + "행: 거래일시가 yyyy.MM.dd HH:mm:ss 형식이 아닙니다");
        }
        long deposit = amount(rowNumber, "입금액", depositText);
        long withdrawal = amount(rowNumber, "출금액", withdrawalText);
        if (balanceText.isEmpty()) {
            throw ImportFileInvalidType.of(rowNumber + "행: 잔액이 비어 있습니다");
        }
        long balance = amount(rowNumber, "잔액", balanceText);

        EntryType type;
        long amount;
        if (deposit > 0 && withdrawal == 0) {
            type = EntryType.DEPOSIT;
            amount = deposit;
        } else if (deposit == 0 && withdrawal > 0) {
            type = EntryType.WITHDRAWAL;
            amount = withdrawal;
        } else {
            throw ImportFileInvalidType.of(rowNumber + "행: 입금액과 출금액 중 정확히 하나만 0보다 커야 합니다");
        }

        String counterparty = normalizeCounterparty(content);
        if (counterparty.length() > MAX_COUNTERPARTY_CHARS) {
            throw ImportFileInvalidType.of(rowNumber + "행: 내용이 100자를 넘습니다");
        }
        return new ParsedTransaction(rowNumber, occurredAt, type, amount, balance, counterparty);
    }

    /** 상대(내용 열)는 앞뒤 공백을 지우고 NFC로 맞춘다. 비어 있으면 "(내용 없음)"이다. rowKey 계산에도 같은 값을 쓴다. */
    public static String normalizeCounterparty(String content) {
        String normalized = Normalizer.normalize(content == null ? "" : content.strip(), Normalizer.Form.NFC);
        return normalized.isEmpty() ? EMPTY_COUNTERPARTY : normalized;
    }

    /** 빈칸은 0. 숫자 셀("12345.0")과 쉼표 문자열("12,345") 모두 정수여야 한다. */
    private static long amount(int rowNumber, String column, String text) {
        if (text.isEmpty()) {
            return 0;
        }
        try {
            BigDecimal value;
            if (COMMA_INTEGER.matcher(text).matches() && text.contains(",")) {
                value = new BigDecimal(text.replace(",", ""));
            } else {
                value = new BigDecimal(text);
            }
            if (value.signum() < 0 && !"잔액".equals(column)) {
                throw new NumberFormatException();
            }
            return value.stripTrailingZeros().scale() > 0 ? fail(rowNumber, column) : value.longValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            return fail(rowNumber, column);
        }
    }

    private static long fail(int rowNumber, String column) {
        throw ImportFileInvalidType.of(rowNumber + "행: " + column + "이 원 단위 정수가 아닙니다");
    }

    private static Map<ShinhanColumn, Integer> headerColumns(Row row) {
        Map<ShinhanColumn, Integer> columns = new EnumMap<>(ShinhanColumn.class);
        row.cells.forEach((index, text) -> ShinhanColumn.ofHeader(text.strip())
                .ifPresent(column -> columns.putIfAbsent(column, index)));
        return columns.size() == ShinhanColumn.values().length ? columns : null;
    }

    /** 헤더를 못 찾았을 때, 가장 많이 맞은 행 기준으로 빠진 열 이름을 알려 준다. */
    private static String missingColumns(List<Row> rows) {
        Map<ShinhanColumn, Integer> best = new EnumMap<>(ShinhanColumn.class);
        for (int i = 0; i < Math.min(HEADER_SEARCH_ROWS, rows.size()); i++) {
            Map<ShinhanColumn, Integer> found = new EnumMap<>(ShinhanColumn.class);
            rows.get(i).cells.forEach((index, text) -> ShinhanColumn.ofHeader(text.strip())
                    .ifPresent(column -> found.putIfAbsent(column, index)));
            if (found.size() > best.size()) {
                best = found;
            }
        }
        List<String> missing = new ArrayList<>();
        for (ShinhanColumn column : ShinhanColumn.values()) {
            if (!best.containsKey(column)) {
                missing.add(column.header());
            }
        }
        return String.join(", ", missing);
    }

    private static int columnIndex(String cellRef) {
        int index = 0;
        int i = 0;
        while (i < cellRef.length() && Character.isLetter(cellRef.charAt(i))) {
            index = index * 26 + (Character.toUpperCase(cellRef.charAt(i)) - 'A' + 1);
            i++;
        }
        if (index == 0) {
            throw new NumberFormatException();
        }
        return index - 1;
    }

    private static void appendLimited(StringBuilder builder, String text) {
        if (builder.length() + text.length() > MAX_CELL_CHARS) {
            throw ImportFileInvalidType.of("셀 문자열이 너무 깁니다");
        }
        builder.append(text);
    }

    /** DTD와 외부 엔티티를 끈 StAX 리더(XXE 차단). JDK 기본 구현을 쓴다. */
    private static XMLStreamReader open(byte[] xml) {
        XMLInputFactory factory = XMLInputFactory.newDefaultFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        try {
            return factory.createXMLStreamReader(new ByteArrayInputStream(xml));
        } catch (XMLStreamException e) {
            throw ImportFileInvalidType.of("xlsx 구조를 읽을 수 없습니다");
        }
    }

    private static final class Row {

        private final int number;
        private final Map<Integer, String> cells = new HashMap<>();

        private Row(int number) {
            this.number = number;
        }

        private String text(Integer column) {
            String value = column == null ? null : cells.get(column);
            return value == null ? "" : value.strip();
        }
    }

    /** 파일에서 읽은 거래 한 건. rowNumber는 시트의 행 번호다(오류 안내용). */
    public record ParsedTransaction(int rowNumber, LocalDateTime occurredAt, EntryType type, long amount,
                                    long bankBalance, String counterparty) {
    }

    /** 열은 위치가 아니라 헤더 이름으로 찾는다. A열(No)과 손으로 추가한 G~I열은 쓰지 않는다. */
    enum ShinhanColumn {
        OCCURRED_AT("거래일시"),
        DEPOSIT("입금액"),
        WITHDRAWAL("출금액"),
        CONTENT("내용"),
        BALANCE("잔액");

        private final String header;

        ShinhanColumn(String header) {
            this.header = header;
        }

        String header() {
            return header;
        }

        static java.util.Optional<ShinhanColumn> ofHeader(String text) {
            for (ShinhanColumn column : values()) {
                if (column.header.equals(text)) {
                    return java.util.Optional.of(column);
                }
            }
            return java.util.Optional.empty();
        }
    }
}
