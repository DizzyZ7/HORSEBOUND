// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

record GameSettings(boolean vsync, float mouseSensitivity, int autosaveSeconds) {
    static final float MIN_SENSITIVITY = 0.05f;
    static final float MAX_SENSITIVITY = 0.40f;
    static final int MIN_AUTOSAVE_SECONDS = 30;
    static final int MAX_AUTOSAVE_SECONDS = 300;

    GameSettings {
        if (!Float.isFinite(mouseSensitivity)) {
            mouseSensitivity = 0.16f;
        }
        mouseSensitivity = Math.max(MIN_SENSITIVITY, Math.min(MAX_SENSITIVITY, mouseSensitivity));
        autosaveSeconds = Math.max(MIN_AUTOSAVE_SECONDS, Math.min(MAX_AUTOSAVE_SECONDS, autosaveSeconds));
    }

    static GameSettings defaults() {
        return new GameSettings(true, 0.16f, 60);
    }

    GameSettings withVsync(boolean enabled) {
        return new GameSettings(enabled, mouseSensitivity, autosaveSeconds);
    }

    GameSettings withMouseSensitivity(float value) {
        return new GameSettings(vsync, value, autosaveSeconds);
    }

    GameSettings withAutosaveSeconds(int value) {
        return new GameSettings(vsync, mouseSensitivity, value);
    }
}
