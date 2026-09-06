package com.bcsdlab.bcsdinternalapiv2.game.util;

import java.net.URI;
import java.net.URISyntaxException;

public final class GameBuildUrlNormalizer {

    private GameBuildUrlNormalizer() {
    }

    public static String normalize(String buildFileUrl, String publicOrigin) {
        if (buildFileUrl == null) {
            return null;
        }

        try {
            URI current = URI.create(buildFileUrl);
            if (current.getPath() == null || !current.getPath().startsWith("/games/")) {
                return buildFileUrl;
            }

            URI origin = URI.create(publicOrigin);
            return new URI(origin.getScheme(), origin.getAuthority(), current.getPath(),
                    current.getQuery(), current.getFragment()).toString();
        } catch (IllegalArgumentException | URISyntaxException e) {
            return buildFileUrl;
        }
    }
}
