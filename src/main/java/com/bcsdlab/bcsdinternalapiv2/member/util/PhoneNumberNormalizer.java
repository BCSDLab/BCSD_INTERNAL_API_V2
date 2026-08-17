package com.bcsdlab.bcsdinternalapiv2.member.util;

import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import java.util.regex.Pattern;

public final class PhoneNumberNormalizer {

    private static final Pattern NORMALIZED_PATTERN = Pattern.compile("^01[016789][0-9]{7,8}$");

    private PhoneNumberNormalizer() {
    }

    public static String normalize(String rawPhoneNumber) {
        String digitsOnly = rawPhoneNumber.replaceAll("[^0-9]", "");
        if (!NORMALIZED_PATTERN.matcher(digitsOnly).matches()) {
            throw new MemberException(MemberExceptionType.INVALID_PHONE);
        }
        return digitsOnly;
    }
}
