package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * 장부 증빙으로 받는 파일 형식과 확장자 짝. FE 선택기 accept와 같다. SVG(스크립트 실행 위험)·GIF·HEIC는 받지 않는다.
 * 서버는 파일 바이트를 보지 않으므로, 형식은 presign 서명(Content-Type 고정)과 업로드 확인으로 지킨다.
 */
public enum EvidenceFormat {
    PNG("image/png", Set.of("png")),
    JPEG("image/jpeg", Set.of("jpg", "jpeg")),
    WEBP("image/webp", Set.of("webp")),
    PDF("application/pdf", Set.of("pdf"));

    /** 증빙 하나의 최대 크기(10MB). 발급 검증과 업로드 확인이 같은 값을 쓴다. */
    public static final long MAX_BYTES = 10L * 1024 * 1024;

    /** 장부 기록 하나에 붙일 수 있는 증빙 수. */
    public static final int MAX_PER_ENTRY = 5;

    private final String contentType;
    private final Set<String> extensions;

    EvidenceFormat(String contentType, Set<String> extensions) {
        this.contentType = contentType;
        this.extensions = extensions;
    }

    public static Optional<EvidenceFormat> ofContentType(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        String normalized = baseType(contentType);
        return Arrays.stream(values()).filter(format -> format.contentType.equals(normalized)).findFirst();
    }

    public boolean acceptsFileName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 && extensions.contains(fileName.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    public String contentType() {
        return contentType;
    }

    /** "image/png; charset=..." 같은 매개변수를 떼고 소문자로 맞춘다. */
    public static String baseType(String contentType) {
        int semicolon = contentType.indexOf(';');
        String base = semicolon >= 0 ? contentType.substring(0, semicolon) : contentType;
        return base.strip().toLowerCase(Locale.ROOT);
    }
}
