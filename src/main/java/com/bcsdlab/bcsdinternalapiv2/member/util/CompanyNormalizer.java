package com.bcsdlab.bcsdinternalapiv2.member.util;

public final class CompanyNormalizer {

    private CompanyNormalizer() {
    }

    /** 직장은 앞뒤 공백을 지우고, 비어 있으면 null로 저장한다. */
    public static String normalize(String rawCompany) {
        if (rawCompany == null || rawCompany.isBlank()) {
            return null;
        }
        return rawCompany.trim();
    }
}
