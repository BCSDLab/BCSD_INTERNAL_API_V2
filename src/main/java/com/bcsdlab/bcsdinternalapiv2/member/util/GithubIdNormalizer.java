package com.bcsdlab.bcsdinternalapiv2.member.util;

import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import java.util.regex.Pattern;

public final class GithubIdNormalizer {

    private static final Pattern GITHUB_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9]|-(?=[A-Za-z0-9])){0,38}$");
    private static final String[] URL_PREFIXES = {
            "https://github.com/", "http://github.com/", "github.com/"
    };

    private GithubIdNormalizer() {
    }

    public static String normalize(String rawGithubId) {
        if (rawGithubId == null || rawGithubId.isBlank()) {
            return null;
        }
        String trimmed = rawGithubId.trim();
        for (String prefix : URL_PREFIXES) {
            if (trimmed.startsWith(prefix)) {
                trimmed = trimmed.substring(prefix.length());
                break;
            }
        }
        trimmed = trimmed.replaceAll("/$", "");
        if (!GITHUB_ID_PATTERN.matcher(trimmed).matches()) {
            throw new MemberException(MemberExceptionType.INVALID_GITHUB_ID);
        }
        return trimmed;
    }
}
