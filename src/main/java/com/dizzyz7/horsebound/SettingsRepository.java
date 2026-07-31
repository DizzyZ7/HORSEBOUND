// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

final class SettingsRepository {
    private final Path path;

    SettingsRepository() {
        this(AppPaths.userDataRoot().resolve("settings.properties"));
    }

    SettingsRepository(Path path) {
        this.path = path;
    }

    GameSettings load() {
        if (!Files.isRegularFile(path)) {
            return GameSettings.defaults();
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            boolean vsync = Boolean.parseBoolean(properties.getProperty("vsync", "true"));
            float sensitivity = parseFloat(properties.getProperty("mouseSensitivity"), 0.16f);
            int autosave = parseInt(properties.getProperty("autosaveSeconds"), 60);
            return new GameSettings(vsync, sensitivity, autosave);
        } catch (IOException | RuntimeException ex) {
            return GameSettings.defaults();
        }
    }

    void save(GameSettings settings) {
        Path parent = path.getParent();
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Properties properties = new Properties();
        properties.setProperty("vsync", Boolean.toString(settings.vsync()));
        properties.setProperty("mouseSensitivity", Float.toString(settings.mouseSensitivity()));
        properties.setProperty("autosaveSeconds", Integer.toString(settings.autosaveSeconds()));

        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream out = Files.newOutputStream(temporary)) {
                properties.store(out, "HORSEBOUND settings - Created by Dimash Janibekov (DizZyZ7)");
            }
            moveAtomically(temporary, path);
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // Keep the original failure.
            }
            throw new SettingsException("Could not save HORSEBOUND settings to " + path, ex);
        }
    }

    Path path() {
        return path;
    }

    private static float parseFloat(String raw, float fallback) {
        try {
            return raw == null ? fallback : Float.parseFloat(raw);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static int parseInt(String raw, int fallback) {
        try {
            return raw == null ? fallback : Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static final class SettingsException extends RuntimeException {
        SettingsException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
