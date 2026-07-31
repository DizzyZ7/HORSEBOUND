// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

record BuildInfo(String version, String commit, String vendor) {
    private static final String UNKNOWN = "unknown";

    BuildInfo {
        version = normalized(version, "development");
        commit = normalized(commit, "local");
        vendor = normalized(vendor, AuthorInfo.CREATOR);
    }

    static BuildInfo current() {
        ClassLoader loader = BuildInfo.class.getClassLoader();
        try {
            Enumeration<URL> manifests = loader.getResources("META-INF/MANIFEST.MF");
            while (manifests.hasMoreElements()) {
                URL url = manifests.nextElement();
                try (InputStream input = url.openStream()) {
                    Attributes attributes = new Manifest(input).getMainAttributes();
                    if (!AuthorInfo.GAME_NAME.equals(attributes.getValue("Implementation-Title"))) {
                        continue;
                    }
                    return new BuildInfo(
                        attributes.getValue("Implementation-Version"),
                        attributes.getValue("Build-Commit"),
                        attributes.getValue("Implementation-Vendor")
                    );
                }
            }
        } catch (IOException ignored) {
            // Development runs may not expose a packaged manifest.
        }

        Package gamePackage = BuildInfo.class.getPackage();
        return new BuildInfo(
            gamePackage == null ? null : gamePackage.getImplementationVersion(),
            System.getProperty("horsebound.build.commit"),
            gamePackage == null ? null : gamePackage.getImplementationVendor()
        );
    }

    String displayLabel() {
        return AuthorInfo.GAME_NAME + " " + version;
    }

    String diagnosticLabel() {
        return displayLabel() + " (" + commit + ")";
    }

    private static String normalized(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? UNKNOWN : trimmed;
    }
}
