package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.ImportFileInvalidType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * xlsx(zip)를 풀면서 압축 해제 한도를 검사한다(설계 5-5절, 검토 blocker 5). zip 헤더에 적힌 크기는 믿지 않고,
 * 실제로 읽은 바이트를 센다. 하나라도 넘으면 즉시 멈추고 IMPORT_FILE_INVALID다.
 *
 * <p>엔트리를 모두 끝까지 읽는다(필요 없는 엔트리도 한도 계산에 넣기 위해서). 결과는 엔트리 이름 → 내용이다.
 * 합계 한도가 16MB라 메모리에 들고 있어도 된다.
 */
final class XlsxZipGuard {

    static final int MAX_ENTRIES = 50;
    static final long MAX_ENTRY_BYTES = 8L * 1024 * 1024;
    static final long MAX_TOTAL_BYTES = 16L * 1024 * 1024;
    static final long MAX_RATIO = 200;
    /** 아주 작은 엔트리는 압축률이 높게 나와도 위험하지 않아, 이 크기를 넘은 뒤부터 압축률을 본다. */
    static final long RATIO_CHECK_FROM_BYTES = 64L * 1024;

    private XlsxZipGuard() {
    }

    static Map<String, byte[]> unzip(byte[] file) {
        CountingInputStream raw = new CountingInputStream(new ByteArrayInputStream(file));
        Map<String, byte[]> entries = new HashMap<>();
        long total = 0;
        int count = 0;
        byte[] buffer = new byte[8192];
        try (ZipInputStream zip = new ZipInputStream(raw)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++count > MAX_ENTRIES) {
                    throw ImportFileInvalidType.of("압축 파일 안의 항목이 너무 많습니다");
                }
                long compressedStart = raw.count();
                long entryBytes = 0;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int read;
                while ((read = zip.read(buffer)) > 0) {
                    entryBytes += read;
                    total += read;
                    if (entryBytes > MAX_ENTRY_BYTES || total > MAX_TOTAL_BYTES) {
                        throw ImportFileInvalidType.of("압축을 푼 크기가 너무 큽니다");
                    }
                    long compressed = Math.max(1, raw.count() - compressedStart);
                    if (entryBytes > RATIO_CHECK_FROM_BYTES && entryBytes / compressed > MAX_RATIO) {
                        throw ImportFileInvalidType.of("압축률이 비정상적입니다");
                    }
                    out.write(buffer, 0, read);
                }
                if (!entry.isDirectory()) {
                    entries.put(entry.getName(), out.toByteArray());
                }
            }
        } catch (ZipException e) {
            throw ImportFileInvalidType.of("xlsx 파일이 아닙니다");
        } catch (IOException e) {
            throw ImportFileInvalidType.of("파일을 읽을 수 없습니다");
        }
        if (entries.isEmpty()) {
            throw ImportFileInvalidType.of("xlsx 파일이 아닙니다");
        }
        return entries;
    }

    /** zip 스트림이 실제로 가져간 압축 바이트를 센다. */
    private static final class CountingInputStream extends FilterInputStream {

        private long count;

        CountingInputStream(InputStream in) {
            super(in);
        }

        long count() {
            return count;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                count++;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n > 0) {
                count += n;
            }
            return n;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = super.skip(n);
            count += skipped;
            return skipped;
        }
    }
}
