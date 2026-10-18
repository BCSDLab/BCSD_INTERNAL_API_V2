package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.ImportFileInvalidType;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * xlsx의 첫 시트를 행 목록으로 읽는다(설계 5-5절). 라이브러리 없이 JDK zip + StAX만 쓴다.
 * 은행 형식은 모른다 — 헤더·금액 해석은 {@link ShinhanXlsxParser}가 한다.
 *
 * <ul>
 *   <li>읽는 엔트리: workbook.xml → workbook.xml.rels로 첫 시트 경로, sharedStrings.xml(있으면), 첫 시트</li>
 *   <li>문자열은 sharedStrings(t="s", 엑셀 재저장본)와 inlineStr(은행 원본) 둘 다 읽는다</li>
 *   <li>숫자 셀은 원문 그대로("12345.0") 둔다</li>
 * </ul>
 */
final class XlsxSheetReader {

    static final int MAX_SHEET_ROWS = 20_000;
    static final int MAX_CELL_CHARS = 1_000;
    private static final String REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

    private XlsxSheetReader() {
    }

    static List<SheetRow> readFirstSheet(byte[] file) {
        Map<String, byte[]> entries = XlsxZipGuard.unzip(file);
        String sheetPath = firstSheetPath(entries);
        byte[] sheet = entries.get(sheetPath);
        if (sheet == null) {
            throw ImportFileInvalidType.of("시트를 찾을 수 없습니다");
        }
        byte[] shared = entries.get("xl/sharedStrings.xml");
        List<String> sharedStrings = shared == null ? List.of() : readSharedStrings(shared);
        return readRows(sheet, sharedStrings);
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

    /** 시트의 행을 (열 번호 → 셀 문자열)로 읽는다. */
    private static List<SheetRow> readRows(byte[] xml, List<String> sharedStrings) {
        List<SheetRow> rows = new ArrayList<>();
        XMLStreamReader reader = open(xml);
        try {
            SheetRow row = null;
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
                            row = new SheetRow(r == null ? rows.size() + 1 : Integer.parseInt(r));
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

    /** 시트의 한 행. number는 시트에 적힌 행 번호다(오류 안내용). */
    static final class SheetRow {

        private final int number;
        private final Map<Integer, String> cells = new HashMap<>();

        private SheetRow(int number) {
            this.number = number;
        }

        int number() {
            return number;
        }

        Map<Integer, String> cells() {
            return cells;
        }

        /** 열의 값을 앞뒤 공백을 지워 돌려준다. 열이 없거나 셀이 비면 빈 문자열이다. */
        String text(Integer column) {
            String value = column == null ? null : cells.get(column);
            return value == null ? "" : value.strip();
        }
    }
}
