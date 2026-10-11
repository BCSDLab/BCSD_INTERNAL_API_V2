package com.bcsdlab.bcsdinternalapiv2.ledger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 합성 신한 거래내역 xlsx(설계 5-5절 "테스트 fixture"). 실제 파일은 레포에 넣지 않으므로 구조만 흉내 낸다.
 * 이름은 "회원1" 같은 가짜, 금액도 가짜다.
 *
 * <ul>
 *   <li>엑셀 재저장본: sharedStrings, 빈 행 패딩, 합계 1행 → {@link #excelResaved}</li>
 *   <li>은행 원본: inlineStr, 합계 2행 → {@link #bankOriginal}</li>
 *   <li>쉼표 문자열 금액 → {@link #commaAmounts()}</li>
 *   <li>추가 열(G~I)과 A1 이상치 → {@link #extraColumns()}</li>
 * </ul>
 */
final class XlsxFixture {

    private boolean sharedStrings;
    private boolean commaAmounts;
    private boolean extraColumns;
    private String a1 = "No";
    private int paddingRows;
    private int totalRows;
    private final List<Tx> rows = new ArrayList<>();
    private final List<String[]> rawRows = new ArrayList<>();

    static XlsxFixture excelResaved() {
        XlsxFixture fixture = new XlsxFixture();
        fixture.sharedStrings = true;
        fixture.paddingRows = 30;
        fixture.totalRows = 1;
        return fixture;
    }

    static XlsxFixture bankOriginal() {
        XlsxFixture fixture = new XlsxFixture();
        fixture.totalRows = 2;
        return fixture;
    }

    XlsxFixture commaAmounts() {
        this.commaAmounts = true;
        return this;
    }

    XlsxFixture extraColumns() {
        this.extraColumns = true;
        this.a1 = "";
        return this;
    }

    XlsxFixture a1(String value) {
        this.a1 = value;
        return this;
    }

    XlsxFixture padding(int rows) {
        this.paddingRows = rows;
        return this;
    }

    XlsxFixture deposit(String at, long amount, String content, long balance) {
        rows.add(new Tx(at, amount, 0, content, balance));
        return this;
    }

    XlsxFixture withdrawal(String at, long amount, String content, long balance) {
        rows.add(new Tx(at, 0, amount, content, balance));
        return this;
    }

    /** 그대로 넣는 행(B~F 문자열). 비정상 행 테스트용. */
    XlsxFixture raw(String at, String deposit, String withdrawal, String content, String balance) {
        rawRows.add(new String[] {at, deposit, withdrawal, content, balance});
        return this;
    }

    byte[] build() {
        return zip(parts(sheetXml()));
    }

    /** 기본 구성(workbook·rels·content types + 주어진 시트 XML). */
    Map<String, byte[]> parts(String sheetXml) {
        Map<String, byte[]> parts = new LinkedHashMap<>();
        parts.put("[Content_Types].xml", utf8("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                <Default Extension="xml" ContentType="application/xml"/>
                </Types>"""));
        parts.put("xl/workbook.xml", utf8("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                 xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                <sheets><sheet name="sheet" sheetId="1" r:id="rId1"/></sheets></workbook>"""));
        parts.put("xl/_rels/workbook.xml.rels", utf8("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                </Relationships>"""));
        parts.put("xl/worksheets/sheet1.xml", utf8(sheetXml));
        if (sharedStrings) {
            parts.put("xl/sharedStrings.xml", utf8(sharedStringsXml()));
        }
        return parts;
    }

    private final List<String> strings = new ArrayList<>();

    private String sheetXml() {
        strings.clear();
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""");
        int r = 1;
        List<String> header = new ArrayList<>(List.of(a1, "거래일시", "입금액", "출금액", "내용", "잔액"));
        if (extraColumns) {
            header.addAll(List.of("메모", "입금인코드", ""));
        }
        xml.append("<row r=\"").append(r).append("\">");
        for (int c = 0; c < header.size(); c++) {
            if (!header.get(c).isEmpty()) {
                xml.append(text(col(c) + r, header.get(c)));
            }
        }
        xml.append("</row>");

        long depositSum = 0;
        long withdrawalSum = 0;
        int no = 1;
        for (Tx tx : rows) {
            r++;
            xml.append("<row r=\"").append(r).append("\">")
                    .append(number("A" + r, no++))
                    .append(text("B" + r, tx.at))
                    .append(amount("C" + r, tx.deposit))
                    .append(amount("D" + r, tx.withdrawal))
                    .append(text("E" + r, tx.content))
                    .append(amount("F" + r, tx.balance));
            if (extraColumns) {
                xml.append(text("G" + r, "메모")).append(text("H" + r, "코드"));
            }
            xml.append("</row>");
            depositSum += tx.deposit;
            withdrawalSum += tx.withdrawal;
        }
        for (String[] raw : rawRows) {
            r++;
            xml.append("<row r=\"").append(r).append("\">").append(number("A" + r, no++));
            for (int c = 0; c < raw.length; c++) {
                if (raw[c] != null) {
                    xml.append(text(col(c + 1) + r, raw[c]));
                }
            }
            xml.append("</row>");
        }
        if (totalRows == 1) {
            r++;
            xml.append("<row r=\"").append(r).append("\">").append(text("B" + r, "합계"))
                    .append(amount("C" + r, depositSum)).append(amount("D" + r, withdrawalSum)).append("</row>");
        } else if (totalRows == 2) {
            for (int i = 0; i < 2; i++) {
                r++;
                xml.append("<row r=\"").append(r).append("\">").append(text("B" + r, "합계")).append("</row>");
            }
        }
        for (int i = 0; i < paddingRows; i++) {
            r++;
            xml.append("<row r=\"").append(r).append("\"><c r=\"A").append(r).append("\" s=\"1\"/></row>");
        }
        return xml.append("</sheetData></worksheet>").toString();
    }

    private String sharedStringsXml() {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""");
        for (String value : strings) {
            xml.append("<si><t>").append(escape(value)).append("</t></si>");
        }
        return xml.append("</sst>").toString();
    }

    private String text(String ref, String value) {
        if (sharedStrings) {
            strings.add(value);
            return "<c r=\"" + ref + "\" t=\"s\"><v>" + (strings.size() - 1) + "</v></c>";
        }
        return "<c r=\"" + ref + "\" t=\"inlineStr\"><is><t>" + escape(value) + "</t></is></c>";
    }

    private String amount(String ref, long value) {
        if (commaAmounts) {
            return text(ref, String.format("%,d", value));
        }
        return "<c r=\"" + ref + "\"><v>" + value + ".0</v></c>";
    }

    private static String number(String ref, long value) {
        return "<c r=\"" + ref + "\"><v>" + value + "</v></c>";
    }

    private static String col(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static byte[] utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    static byte[] zip(Map<String, byte[]> parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, byte[]> part : parts.entrySet()) {
                zip.putNextEntry(new ZipEntry(part.getKey()));
                zip.write(part.getValue());
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return out.toByteArray();
    }

    private record Tx(String at, long deposit, long withdrawal, String content, long balance) {
    }
}
