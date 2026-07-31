// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.nio.file.Path;

final class AppPaths {
    private AppPaths() {
    }

    static Path userDataRoot() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, "HORSEBOUND");
        }
        return Path.of(System.getProperty("user.home"), ".horsebound");
    }
}
