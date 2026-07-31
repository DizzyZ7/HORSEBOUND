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
        if (!Files.isRegularFile(path)) return GameSettings.defaults();

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            GameSettings defaults = GameSettings.defaults();
            boolean vsync = Boolean.parseBoolean(properties.getProperty("vsync", Boolean.toString(defaults.vsync())));
            float sensitivity = parseFloat(properties.getProperty("mouseSensitivity"), defaults.mouseSensitivity());
            int autosave = parseInt(properties.getProperty("autosaveSeconds"), defaults.autosaveSeconds());
            WindowMode windowMode = parseEnum(
                WindowMode.class,
                properties.getProperty("windowMode"),
                defaults.windowMode()
            );
            int windowWidth = parseInt(properties.getProperty("windowWidth"), defaults.windowWidth());
            int windowHeight = parseInt(properties.getProperty("windowHeight"), defaults.windowHeight());
            float uiScale = parseFloat(properties.getProperty("uiScale"), defaults.uiScale());
            GraphicsPreset graphicsPreset = parseEnum(
                GraphicsPreset.class,
                properties.getProperty("graphicsPreset"),
                defaults.graphicsPreset()
            );
            boolean performanceStats = Boolean.parseBoolean(
                properties.getProperty("showPerformanceStats", Boolean.toString(defaults.showPerformanceStats()))
            );
            return new GameSettings(
                vsync,
                sensitivity,
                autosave,
                windowMode,
                windowWidth,
                windowHeight,
                uiScale,
                graphicsPreset,
                performanceStats
            );
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
        properties.setProperty("windowMode", settings.windowMode().name());
        properties.setProperty("windowWidth", Integer.toString(settings.windowWidth()));
        properties.setProperty("windowHeight", Integer.toString(settings.windowHeight()));
        properties.setProperty("uiScale", Float.toString(settings.uiScale()));
        properties.setProperty("graphicsPreset", settings.graphicsPreset().name());
        properties.setProperty("showPerformanceStats", Boolean.toString(settings.showPerformanceStats()));

        try {
            if (parent != null) Files.createDirectories(parent);
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

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String raw, E fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
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
