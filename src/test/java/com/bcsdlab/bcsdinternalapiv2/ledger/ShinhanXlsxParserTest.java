package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.ImportFileInvalidType;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer.Movement;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ImportSequencer.Sequenced;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.RowKeys;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ShinhanXlsxParser;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.ShinhanXlsxParser.ParsedTransaction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** 합성 fixture(설계 5-5절 6종)로 파서·압축 해제 한도·같은 초 순서·rowKey를 확인한다. 실제 파일은 쓰지 않는다. */
class ShinhanXlsxParserTest {

    @Nested
    class 형식_변형 {

        @Test
        void 엑셀_재저장본_sharedStrings_빈_행_패딩_합계_1행() {
            byte[] file = XlsxFixture.excelResaved()
                    .deposit("2026.09.01 10:00:00", 10_000, "회원1", 110_000)
                    .withdrawal("2026.09.02 11:30:15", 3_000, "가게1", 107_000)
                    .build();

            List<ParsedTransaction> parsed = ShinhanXlsxParser.parse(file);

            assertThat(parsed).containsExactly(
                    new ParsedTransaction(2, LocalDateTime.of(2026, 9, 1, 10, 0, 0), EntryType.DEPOSIT, 10_000, 110_000,
                            "회원1"),
                    new ParsedTransaction(3, LocalDateTime.of(2026, 9, 2, 11, 30, 15), EntryType.WITHDRAWAL, 3_000,
                            107_000, "가게1"));
        }

        @Test
        void 은행_원본_inlineStr_합계_2행() {
            byte[] file = XlsxFixture.bankOriginal()
                    .deposit("2026.10.01 09:00:00", 20_000, "회원2", 120_000)
                    .build();

            assertThat(ShinhanXlsxParser.parse(file)).singleElement()
                    .satisfies(t -> {
                        assertThat(t.type()).isEqualTo(EntryType.DEPOSIT);
                        assertThat(t.amount()).isEqualTo(20_000);
                        assertThat(t.bankBalance()).isEqualTo(120_000);
                        assertThat(t.counterparty()).isEqualTo("회원2");
                    });
        }

        @Test
        void 금액이_쉼표_문자열이어도_정수로_읽는다() {
            byte[] file = XlsxFixture.excelResaved().commaAmounts()
                    .deposit("2024.11.05 12:00:00", 1_234_567, "회원3", 9_876_543)
                    .build();

            assertThat(ShinhanXlsxParser.parse(file)).singleElement()
                    .satisfies(t -> {
                        assertThat(t.amount()).isEqualTo(1_234_567);
                        assertThat(t.bankBalance()).isEqualTo(9_876_543);
                    });
        }

        @Test
        void 추가_열_G_I와_A1_이상치는_무시한다() {
            byte[] file = XlsxFixture.excelResaved().extraColumns()
                    .withdrawal("2025.01.10 08:00:00", 5_000, "가게2", 50_000)
                    .build();
            byte[] otherA1 = XlsxFixture.bankOriginal().a1("번호?")
                    .withdrawal("2025.01.10 08:00:00", 5_000, "가게2", 50_000)
                    .build();

            assertThat(ShinhanXlsxParser.parse(file)).singleElement()
                    .satisfies(t -> assertThat(t.counterparty()).isEqualTo("가게2"));
            assertThat(ShinhanXlsxParser.parse(otherA1)).hasSize(1);
        }

        @Test
        void 내용은_공백을_지우고_NFC로_맞추며_비면_내용_없음이다() {
            byte[] file = XlsxFixture.bankOriginal()
                    .deposit("2026.09.01 10:00:00", 1_000, "  " + Normalizer.normalize("회원4", Normalizer.Form.NFD) + " ",
                            1_000)
                    .deposit("2026.09.01 10:00:01", 1_000, "", 2_000)
                    .build();

            List<ParsedTransaction> parsed = ShinhanXlsxParser.parse(file);
            assertThat(parsed.get(0).counterparty()).isEqualTo("회원4");
            assertThat(parsed.get(1).counterparty()).isEqualTo("(내용 없음)");
        }

        @Test
        void 헤더가_첫_행이_아니어도_10행_안이면_찾는다() {
            String sheet = """
                    <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>
                    <row r="1"><c r="A1" t="inlineStr"><is><t>계좌</t></is></c></row>
                    <row r="2"><c r="B2" t="inlineStr"><is><t>거래일시</t></is></c><c r="C2" t="inlineStr"><is><t>입금액</t></is></c>
                    <c r="D2" t="inlineStr"><is><t>출금액</t></is></c><c r="E2" t="inlineStr"><is><t>내용</t></is></c>
                    <c r="F2" t="inlineStr"><is><t>잔액</t></is></c></row>
                    <row r="3"><c r="B3" t="inlineStr"><is><t>2026.09.01 10:00:00</t></is></c><c r="C3"><v>100.0</v></c>
                    <c r="D3"><v>0.0</v></c><c r="E3" t="inlineStr"><is><t>회원5</t></is></c><c r="F3"><v>100.0</v></c></row>
                    </sheetData></worksheet>""";

            assertThat(ShinhanXlsxParser.parse(withSheet(sheet))).singleElement()
                    .satisfies(t -> assertThat(t.rowNumber()).isEqualTo(3));
        }
    }

    @Nested
    class 비정상_파일 {

        @Test
        void 빠진_열이_있으면_열_이름을_알려_준다() {
            String sheet = """
                    <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>
                    <row r="1"><c r="B1" t="inlineStr"><is><t>거래일시</t></is></c><c r="C1" t="inlineStr"><is><t>입금액</t></is></c>
                    <c r="D1" t="inlineStr"><is><t>출금액</t></is></c></row></sheetData></worksheet>""";

            assertInvalid(withSheet(sheet)).contains("빠진 열: 내용, 잔액");
        }

        @Test
        void 거래일시_형식이_아니면_행_번호를_알려_준다() {
            byte[] file = XlsxFixture.bankOriginal().raw("2026-09-01 10:00", "100", "0", "회원", "100").build();

            assertInvalid(file).contains("2행: 거래일시");
        }

        @Test
        void 존재하지_않는_날짜는_거부한다() {
            byte[] file = XlsxFixture.bankOriginal().raw("2026.02.30 10:00:00", "100", "0", "회원", "100").build();

            assertInvalid(file).contains("2행: 거래일시");
        }

        @Test
        void 입금과_출금이_모두_있거나_모두_0이면_거부한다() {
            assertInvalid(XlsxFixture.bankOriginal().raw("2026.09.01 10:00:00", "100", "50", "회원", "100").build())
                    .contains("2행: 입금액과 출금액");
            assertInvalid(XlsxFixture.bankOriginal().raw("2026.09.01 10:00:00", "0", "0", "회원", "100").build())
                    .contains("2행: 입금액과 출금액");
        }

        @Test
        void 금액이_정수가_아니거나_숫자가_아니면_거부한다() {
            assertInvalid(XlsxFixture.bankOriginal().raw("2026.09.01 10:00:00", "100.5", "0", "회원", "100").build())
                    .contains("2행: 입금액");
            assertInvalid(XlsxFixture.bankOriginal().raw("2026.09.01 10:00:00", "1만", "0", "회원", "100").build())
                    .contains("2행: 입금액");
            assertInvalid(XlsxFixture.bankOriginal().raw("2026.09.01 10:00:00", "100", "0", "회원", "").build())
                    .contains("2행: 잔액");
        }

        @Test
        void 오류_메시지에는_셀_값을_넣지_않는다() {
            byte[] file = XlsxFixture.bankOriginal().raw("비밀값-1234", "100", "0", "실명테스트", "100").build();

            assertInvalid(file).doesNotContain("비밀값").doesNotContain("실명테스트");
        }

        @Test
        void zip이_아니면_거부한다() {
            assertInvalid("이것은 엑셀이 아니다".getBytes(StandardCharsets.UTF_8));
            assertInvalid(new byte[0]);
        }

        @Test
        void 시트를_가리키는_항목이_없으면_거부한다() {
            Map<String, byte[]> parts = new LinkedHashMap<>(XlsxFixture.bankOriginal().parts("<worksheet/>"));
            parts.remove("xl/_rels/workbook.xml.rels");

            assertInvalid(XlsxFixture.zip(parts));
        }
    }

    @Nested
    class 압축_해제_한도 {

        @Test
        void 엔트리가_50개를_넘으면_거부한다() {
            Map<String, byte[]> parts = new LinkedHashMap<>(validParts());
            for (int i = 0; i < 50; i++) {
                parts.put("xl/media/dummy" + i + ".bin", new byte[] {1});
            }

            assertInvalid(XlsxFixture.zip(parts)).contains("항목이 너무 많습니다");
        }

        @Test
        void 압축률이_200배를_넘으면_거부한다() {
            Map<String, byte[]> parts = new LinkedHashMap<>(validParts());
            parts.put("xl/media/bomb.bin", new byte[2 * 1024 * 1024]); // 0으로만 채워져 수천 배로 압축된다

            assertInvalid(XlsxFixture.zip(parts)).contains("압축률");
        }

        @Test
        void 엔트리_하나가_8MB를_넘으면_거부한다() {
            Map<String, byte[]> parts = new LinkedHashMap<>(validParts());
            parts.put("xl/media/big.bin", incompressible(8 * 1024 * 1024 + 1));

            assertInvalid(XlsxFixture.zip(parts)).contains("압축을 푼 크기");
        }

        @Test
        void 압축_해제_합계가_16MB를_넘으면_거부한다() {
            Map<String, byte[]> parts = new LinkedHashMap<>(validParts());
            for (int i = 0; i < 3; i++) {
                parts.put("xl/media/part" + i + ".bin", incompressible(6 * 1024 * 1024));
            }

            assertInvalid(XlsxFixture.zip(parts)).contains("압축을 푼 크기");
        }

        @Test
        void 시트_행이_20000개를_넘으면_거부한다() {
            byte[] file = XlsxFixture.bankOriginal().padding(20_000)
                    .deposit("2026.09.01 10:00:00", 100, "회원", 100).build();

            assertInvalid(file).contains("시트의 행이 너무 많습니다");
        }

        @Test
        void 거래가_5000건을_넘으면_거부하고_5000건은_받는다() {
            XlsxFixture five = XlsxFixture.bankOriginal();
            XlsxFixture overflow = XlsxFixture.bankOriginal();
            LocalDateTime at = LocalDateTime.of(2026, 9, 1, 0, 0);
            for (int i = 0; i < 5_001; i++) {
                String time = at.plusSeconds(i).format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
                if (i < 5_000) {
                    five.deposit(time, 1, "회원", i + 1);
                }
                overflow.deposit(time, 1, "회원", i + 1);
            }

            assertThat(ShinhanXlsxParser.parse(five.build())).hasSize(5_000);
            assertInvalid(overflow.build()).contains("5,000건");
        }

        @Test
        void 셀_문자열이_1000자를_넘으면_거부한다() {
            byte[] file = XlsxFixture.bankOriginal().deposit("2026.09.01 10:00:00", 100, "가".repeat(1_001), 100).build();

            assertInvalid(file).contains("셀 문자열");
        }

        @Test
        void 내용이_100자를_넘으면_거부한다() {
            byte[] file = XlsxFixture.bankOriginal().deposit("2026.09.01 10:00:00", 100, "가".repeat(101), 100).build();

            assertInvalid(file).contains("2행: 내용");
        }
    }

    @Nested
    class XXE {

        @Test
        void 외부_엔티티와_DTD는_읽지_않는다() {
            String sheet = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!DOCTYPE worksheet [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                    <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>
                    <row r="1"><c r="B1" t="inlineStr"><is><t>거래일시</t></is></c><c r="C1" t="inlineStr"><is><t>입금액</t></is></c>
                    <c r="D1" t="inlineStr"><is><t>출금액</t></is></c><c r="E1" t="inlineStr"><is><t>내용</t></is></c>
                    <c r="F1" t="inlineStr"><is><t>잔액</t></is></c></row>
                    <row r="2"><c r="B2" t="inlineStr"><is><t>2026.09.01 10:00:00</t></is></c><c r="C2"><v>1.0</v></c>
                    <c r="D2"><v>0.0</v></c><c r="E2" t="inlineStr"><is><t>&xxe;</t></is></c><c r="F2"><v>1.0</v></c></row>
                    </sheetData></worksheet>""";

            assertInvalid(withSheet(sheet)).doesNotContain("root:");
        }

        @Test
        void 문자열_표의_외부_엔티티도_읽지_않는다() {
            Map<String, byte[]> parts = new LinkedHashMap<>(XlsxFixture.excelResaved()
                    .deposit("2026.09.01 10:00:00", 1, "회원", 1).parts(sheetWithSharedIndex()));
            parts.put("xl/sharedStrings.xml", XlsxFixture.utf8("""
                    <?xml version="1.0"?><!DOCTYPE sst [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                    <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><si><t>&xxe;</t></si></sst>"""));

            assertInvalid(XlsxFixture.zip(parts)).doesNotContain("root:");
        }

        private String sheetWithSharedIndex() {
            return """
                    <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>
                    <row r="1"><c r="B1" t="s"><v>0</v></c></row></sheetData></worksheet>""";
        }
    }

    @Nested
    class 같은_초_순서 {

        @Test
        void 파일_순서가_잔액_흐름과_반대인_같은_초_거래는_잔액이_이어지는_순서로_복원한다() {
            // 실제 흐름: 100,000 → +10,000 = 110,000 → -3,000 = 107,000 → +5,000 = 112,000 (모두 같은 초)
            // 파일에는 역순으로 들어 있다.
            byte[] file = XlsxFixture.bankOriginal()
                    .deposit("2026.09.01 09:59:59", 100_000, "기초", 100_000)
                    .deposit("2026.09.01 10:00:00", 5_000, "회원3", 112_000)
                    .withdrawal("2026.09.01 10:00:00", 3_000, "가게", 107_000)
                    .deposit("2026.09.01 10:00:00", 10_000, "회원1", 110_000)
                    .build();

            List<Sequenced<ParsedTransaction>> sequenced = sequence(ShinhanXlsxParser.parse(file));

            assertThat(sequenced).extracting(s -> s.item().counterparty())
                    .containsExactly("기초", "회원1", "가게", "회원3");
            assertThat(sequenced).extracting(Sequenced::seq).containsExactly((short) 0, (short) 0, (short) 1, (short) 2);
        }

        @Test
        void 첫_묶음은_직전_잔액_없이_묶음_안에서만_찾는다() {
            byte[] file = XlsxFixture.bankOriginal()
                    .withdrawal("2026.09.01 10:00:00", 2_000, "가게", 8_000)
                    .deposit("2026.09.01 10:00:00", 5_000, "회원", 10_000)
                    .build();

            assertThat(sequence(ShinhanXlsxParser.parse(file))).extracting(s -> s.item().counterparty())
                    .containsExactly("회원", "가게");
        }

        @Test
        void 맞는_순서가_없으면_파일_순서의_역순을_쓴다() {
            byte[] file = XlsxFixture.bankOriginal()
                    .deposit("2026.09.01 10:00:00", 1_000, "A", 999_999)
                    .deposit("2026.09.01 10:00:00", 1_000, "B", 555_555)
                    .build();

            assertThat(sequence(ShinhanXlsxParser.parse(file))).extracting(s -> s.item().counterparty())
                    .containsExactly("B", "A");
        }

        @Test
        void 다른_초는_시각_순이고_seq는_0이다() {
            byte[] file = XlsxFixture.bankOriginal()
                    .deposit("2026.09.01 10:00:01", 1_000, "B", 2_000)
                    .deposit("2026.09.01 10:00:00", 1_000, "A", 1_000)
                    .build();

            assertThat(sequence(ShinhanXlsxParser.parse(file))).extracting(s -> s.item().counterparty(), Sequenced::seq)
                    .containsExactly(org.assertj.core.groups.Tuple.tuple("A", (short) 0),
                            org.assertj.core.groups.Tuple.tuple("B", (short) 0));
        }
    }

    @Nested
    class rowKey {

        @Test
        void 설계_형식의_SHA_256이고_상대는_정규화해서_넣는다() throws Exception {
            LocalDateTime at = LocalDateTime.of(2026, 11, 16, 9, 10, 0);
            String expected = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest("2026-11-16T09:10:00|DEPOSIT|10000|123456|회원1".getBytes(StandardCharsets.UTF_8)));

            assertThat(RowKeys.of(at, EntryType.DEPOSIT, 10_000, 123_456, "회원1")).isEqualTo(expected);
            assertThat(RowKeys.of(at, EntryType.DEPOSIT, 10_000, 123_456,
                    " " + Normalizer.normalize("회원1", Normalizer.Form.NFD))).isEqualTo(expected);
        }

        @Test
        void 같은_초_같은_금액이어도_잔액이_다르면_다르다() {
            LocalDateTime at = LocalDateTime.of(2026, 11, 16, 9, 10, 0);

            assertThat(RowKeys.of(at, EntryType.DEPOSIT, 10_000, 1, "회원"))
                    .isNotEqualTo(RowKeys.of(at, EntryType.DEPOSIT, 10_000, 2, "회원"))
                    .hasSize(64)
                    .matches("[0-9a-f]{64}");
        }
    }

    private static List<Sequenced<ParsedTransaction>> sequence(List<ParsedTransaction> parsed) {
        return ImportSequencer.sequence(parsed,
                t -> new Movement(t.occurredAt(), t.type(), t.amount(), t.bankBalance()));
    }

    private static Map<String, byte[]> validParts() {
        XlsxFixture fixture = XlsxFixture.bankOriginal().deposit("2026.09.01 10:00:00", 100, "회원", 100);
        Map<String, byte[]> parts = new LinkedHashMap<>();
        try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(
                new java.io.ByteArrayInputStream(fixture.build()))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                parts.put(entry.getName(), zip.readAllBytes());
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        return parts;
    }

    private static byte[] withSheet(String sheet) {
        Map<String, byte[]> parts = new LinkedHashMap<>(validParts());
        parts.put("xl/worksheets/sheet1.xml", XlsxFixture.utf8(sheet));
        return XlsxFixture.zip(parts);
    }

    private static byte[] incompressible(int size) {
        byte[] bytes = new byte[size];
        new Random(42).nextBytes(bytes);
        return bytes;
    }

    /** 셀 값이 메시지에 섞이지 않는지까지 보려고 화면에 나갈 메시지 문자열을 돌려준다. */
    private static AbstractStringAssert<?> assertInvalid(byte[] file) {
        Throwable thrown = catchThrowable(() -> ShinhanXlsxParser.parse(file));
        assertThat(thrown).isInstanceOf(LedgerException.class);
        BcsdExceptionType type = ((LedgerException) thrown).getExceptionType();
        assertThat(type).isInstanceOf(ImportFileInvalidType.class);
        return assertThat(type.getMessage());
    }
}
