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

final class InputProfileRepository {
    private final Path path;

    InputProfileRepository() {
        this(AppPaths.userDataRoot().resolve("input.properties"));
    }

    InputProfileRepository(Path path) {
        this.path = path;
    }

    InputProfile load() {
        if (!Files.isRegularFile(path)) return InputProfile.defaults();
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            InputProfile defaults = InputProfile.defaults();
            InputProfile raw = new InputProfile(
                parseBoolean(properties.getProperty("invertCameraY"), defaults.invertCameraY()),
                parseFloat(properties.getProperty("moveDeadZone"), defaults.moveDeadZone()),
                parseFloat(properties.getProperty("lookDeadZone"), defaults.lookDeadZone()),
                parseEnum(SprintMode.class, properties.getProperty("sprintMode"), defaults.sprintMode()),
                parseBoolean(properties.getProperty("rumbleEnabled"), defaults.rumbleEnabled()),
                parseFloat(properties.getProperty("rumbleStrength"), defaults.rumbleStrength()),
                parseInt(properties.getProperty("key.moveForward"), defaults.moveForwardKey()),
                parseInt(properties.getProperty("key.moveBackward"), defaults.moveBackwardKey()),
                parseInt(properties.getProperty("key.moveLeft"), defaults.moveLeftKey()),
                parseInt(properties.getProperty("key.moveRight"), defaults.moveRightKey()),
                parseInt(properties.getProperty("key.jump"), defaults.jumpKey()),
                parseInt(properties.getProperty("key.interact"), defaults.interactKey()),
                parseInt(properties.getProperty("key.mount"), defaults.mountKey()),
                parseInt(properties.getProperty("key.build"), defaults.buildKey()),
                parseInt(properties.getProperty("key.sprint"), defaults.sprintKey()),
                parseInt(properties.getProperty("key.save"), defaults.saveKey()),
                parseInt(properties.getProperty("key.pause"), defaults.pauseKey())
            );
            return normalizeBindings(raw);
        } catch (IOException | RuntimeException ex) {
            return InputProfile.defaults();
        }
    }

    void save(InputProfile profile) {
        Path parent = path.getParent();
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Properties properties = new Properties();
        properties.setProperty("invertCameraY", Boolean.toString(profile.invertCameraY()));
        properties.setProperty("moveDeadZone", Float.toString(profile.moveDeadZone()));
        properties.setProperty("lookDeadZone", Float.toString(profile.lookDeadZone()));
        properties.setProperty("sprintMode", profile.sprintMode().name());
        properties.setProperty("rumbleEnabled", Boolean.toString(profile.rumbleEnabled()));
        properties.setProperty("rumbleStrength", Float.toString(profile.rumbleStrength()));
        properties.setProperty("key.moveForward", Integer.toString(profile.moveForwardKey()));
        properties.setProperty("key.moveBackward", Integer.toString(profile.moveBackwardKey()));
        properties.setProperty("key.moveLeft", Integer.toString(profile.moveLeftKey()));
        properties.setProperty("key.moveRight", Integer.toString(profile.moveRightKey()));
        properties.setProperty("key.jump", Integer.toString(profile.jumpKey()));
        properties.setProperty("key.interact", Integer.toString(profile.interactKey()));
        properties.setProperty("key.mount", Integer.toString(profile.mountKey()));
        properties.setProperty("key.build", Integer.toString(profile.buildKey()));
        properties.setProperty("key.sprint", Integer.toString(profile.sprintKey()));
        properties.setProperty("key.save", Integer.toString(profile.saveKey()));
        properties.setProperty("key.pause", Integer.toString(profile.pauseKey()));

        try {
            if (parent != null) Files.createDirectories(parent);
            try (OutputStream out = Files.newOutputStream(temporary)) {
                properties.store(out, "HORSEBOUND input profile - Created by Dimash Janibekov (DizZyZ7)");
            }
            moveAtomically(temporary, path);
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // Preserve the original failure.
            }
            throw new InputProfileException("Could not save HORSEBOUND input profile to " + path, ex);
        }
    }

    Path path() {
        return path;
    }

    private static InputProfile normalizeBindings(InputProfile raw) {
        InputProfile defaults = InputProfile.defaults();
        InputProfile normalized = new InputProfile(
            raw.invertCameraY(),
            raw.moveDeadZone(),
            raw.lookDeadZone(),
            raw.sprintMode(),
            raw.rumbleEnabled(),
            raw.rumbleStrength(),
            defaults.moveForwardKey(),
            defaults.moveBackwardKey(),
            defaults.moveLeftKey(),
            defaults.moveRightKey(),
            defaults.jumpKey(),
            defaults.interactKey(),
            defaults.mountKey(),
            defaults.buildKey(),
            defaults.sprintKey(),
            defaults.saveKey(),
            defaults.pauseKey()
        );
        for (BindableAction action : BindableAction.values()) {
            normalized = normalized.withBinding(action, raw.keyFor(action));
        }
        return normalized;
    }

    private static boolean parseBoolean(String raw, boolean fallback) {
        if (raw == null) return fallback;
        if ("true".equalsIgnoreCase(raw.trim())) return true;
        if ("false".equalsIgnoreCase(raw.trim())) return false;
        return fallback;
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

    static final class InputProfileException extends RuntimeException {
        InputProfileException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
