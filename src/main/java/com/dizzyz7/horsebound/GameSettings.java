// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

record GameSettings(
    boolean vsync,
    float mouseSensitivity,
    int autosaveSeconds,
    WindowMode windowMode,
    int windowWidth,
    int windowHeight,
    float uiScale,
    GraphicsPreset graphicsPreset,
    boolean showPerformanceStats,
    float sfxVolume,
    float ambienceVolume,
    Language language
) {
    static final float MIN_SENSITIVITY = 0.05f;
    static final float MAX_SENSITIVITY = 0.40f;
    static final int MIN_AUTOSAVE_SECONDS = 30;
    static final int MAX_AUTOSAVE_SECONDS = 300;
    static final int MIN_WINDOW_WIDTH = 960;
    static final int MAX_WINDOW_WIDTH = 3840;
    static final int MIN_WINDOW_HEIGHT = 600;
    static final int MAX_WINDOW_HEIGHT = 2160;
    static final float MIN_UI_SCALE = 1.00f;
    static final float MAX_UI_SCALE = 1.50f;
    static final float DEFAULT_UI_SCALE = 1.00f;
    static final float MIN_SFX_VOLUME = 0.00f;
    static final float MAX_SFX_VOLUME = 1.00f;
    static final float DEFAULT_SFX_VOLUME = 0.80f;
    static final float MIN_AMBIENCE_VOLUME = 0.00f;
    static final float MAX_AMBIENCE_VOLUME = 1.00f;
    static final float DEFAULT_AMBIENCE_VOLUME = 0.45f;

    GameSettings {
        if (!Float.isFinite(mouseSensitivity)) mouseSensitivity = 0.16f;
        mouseSensitivity = Math.max(MIN_SENSITIVITY, Math.min(MAX_SENSITIVITY, mouseSensitivity));
        autosaveSeconds = Math.max(MIN_AUTOSAVE_SECONDS, Math.min(MAX_AUTOSAVE_SECONDS, autosaveSeconds));
        windowMode = windowMode == null ? WindowMode.WINDOWED : windowMode;
        windowWidth = Math.max(MIN_WINDOW_WIDTH, Math.min(MAX_WINDOW_WIDTH, windowWidth));
        windowHeight = Math.max(MIN_WINDOW_HEIGHT, Math.min(MAX_WINDOW_HEIGHT, windowHeight));
        if (!Float.isFinite(uiScale)) uiScale = DEFAULT_UI_SCALE;
        uiScale = Math.max(MIN_UI_SCALE, Math.min(MAX_UI_SCALE, uiScale));
        graphicsPreset = graphicsPreset == null ? GraphicsPreset.MEDIUM : graphicsPreset;
        if (!Float.isFinite(sfxVolume)) sfxVolume = DEFAULT_SFX_VOLUME;
        sfxVolume = Math.max(MIN_SFX_VOLUME, Math.min(MAX_SFX_VOLUME, sfxVolume));
        if (!Float.isFinite(ambienceVolume)) ambienceVolume = DEFAULT_AMBIENCE_VOLUME;
        ambienceVolume = Math.max(MIN_AMBIENCE_VOLUME, Math.min(MAX_AMBIENCE_VOLUME, ambienceVolume));
        language = language == null ? Language.systemDefault() : language;
    }

    /** Compatibility constructor for settings created before language selection. */
    GameSettings(
        boolean vsync,
        float mouseSensitivity,
        int autosaveSeconds,
        WindowMode windowMode,
        int windowWidth,
        int windowHeight,
        float uiScale,
        GraphicsPreset graphicsPreset,
        boolean showPerformanceStats,
        float sfxVolume,
        float ambienceVolume
    ) {
        this(
            vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight,
            uiScale, graphicsPreset, showPerformanceStats, sfxVolume, ambienceVolume,
            Language.systemDefault()
        );
    }

    GameSettings(
        boolean vsync,
        float mouseSensitivity,
        int autosaveSeconds,
        WindowMode windowMode,
        int windowWidth,
        int windowHeight,
        float uiScale,
        GraphicsPreset graphicsPreset,
        boolean showPerformanceStats,
        float sfxVolume
    ) {
        this(
            vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight,
            uiScale, graphicsPreset, showPerformanceStats, sfxVolume,
            DEFAULT_AMBIENCE_VOLUME, Language.systemDefault()
        );
    }

    GameSettings(
        boolean vsync,
        float mouseSensitivity,
        int autosaveSeconds,
        WindowMode windowMode,
        int windowWidth,
        int windowHeight,
        float uiScale,
        GraphicsPreset graphicsPreset,
        boolean showPerformanceStats
    ) {
        this(
            vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight,
            uiScale, graphicsPreset, showPerformanceStats,
            DEFAULT_SFX_VOLUME, DEFAULT_AMBIENCE_VOLUME, Language.systemDefault()
        );
    }

    GameSettings(boolean vsync, float mouseSensitivity, int autosaveSeconds) {
        this(
            vsync,
            mouseSensitivity,
            autosaveSeconds,
            WindowMode.WINDOWED,
            DisplayResolution.DECK_800.width(),
            DisplayResolution.DECK_800.height(),
            DEFAULT_UI_SCALE,
            GraphicsPreset.MEDIUM,
            false,
            DEFAULT_SFX_VOLUME,
            DEFAULT_AMBIENCE_VOLUME,
            Language.systemDefault()
        );
    }

    static GameSettings defaults() {
        return new GameSettings(true, 0.16f, 60);
    }

    DisplayResolution displayResolution() {
        return DisplayResolution.closest(windowWidth, windowHeight);
    }

    GameSettings withVsync(boolean enabled) {
        return copy(enabled, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight, uiScale, graphicsPreset, showPerformanceStats, sfxVolume, ambienceVolume, language);
    }

    GameSettings withMouseSensitivity(float value) {
        return copy(vsync, value, autosaveSeconds, windowMode, windowWidth, windowHeight, uiScale, graphicsPreset, showPerformanceStats, sfxVolume, ambienceVolume, language);
    }

    GameSettings withAutosaveSeconds(int value) {
        return copy(vsync, mouseSensitivity, value, windowMode, windowWidth, windowHeight, uiScale, graphicsPreset, showPerformanceStats, sfxVolume, ambienceVolume, language);
    }

    GameSettings withWindowMode(WindowMode value) {
        return copy(vsync, mouseSensitivity, autosaveSeconds, value, windowWidth, windowHeight, uiScale, graphicsPreset, showPerformanceStats, sfxVolume, ambienceVolume, language);
    }

    GameSettings withResolution(DisplayResolution resolution) {
        DisplayResolution safe = resolution == null ? DisplayResolution.DECK_800 : resolution;
        return copy(vsync, mouseSensitivity, autosaveSeconds, windowMode, safe.width(), safe.height(), uiScale, graphicsPreset, showPerformanceStats, sfxVolume, ambienceVolume, language);
    }

    GameSettings withUiScale(float value) {
        return copy(vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight, value, graphicsPreset, showPerformanceStats, sfxVolume, ambienceVolume, language);
    }

    GameSettings withGraphicsPreset(GraphicsPreset value) {
        return copy(vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight, uiScale, value, showPerformanceStats, sfxVolume, ambienceVolume, language);
    }

    GameSettings withPerformanceStats(boolean enabled) {
        return copy(vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight, uiScale, graphicsPreset, enabled, sfxVolume, ambienceVolume, language);
    }

    GameSettings withSfxVolume(float value) {
        return copy(vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight, uiScale, graphicsPreset, showPerformanceStats, value, ambienceVolume, language);
    }

    GameSettings withAmbienceVolume(float value) {
        return copy(vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight, uiScale, graphicsPreset, showPerformanceStats, sfxVolume, value, language);
    }

    GameSettings withLanguage(Language value) {
        return copy(vsync, mouseSensitivity, autosaveSeconds, windowMode, windowWidth, windowHeight, uiScale, graphicsPreset, showPerformanceStats, sfxVolume, ambienceVolume, value);
    }

    private static GameSettings copy(
        boolean vsync,
        float sensitivity,
        int autosave,
        WindowMode mode,
        int width,
        int height,
        float uiScale,
        GraphicsPreset preset,
        boolean performanceStats,
        float sfxVolume,
        float ambienceVolume,
        Language language
    ) {
        return new GameSettings(
            vsync, sensitivity, autosave, mode, width, height, uiScale, preset,
            performanceStats, sfxVolume, ambienceVolume, language
        );
    }
}
