// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSettingsDisplayTest {
    @Test
    void legacyConstructorUsesDeckSafeDefaults() {
        GameSettings settings = new GameSettings(true, 0.16f, 60);

        assertEquals(WindowMode.WINDOWED, settings.windowMode());
        assertEquals(1280, settings.windowWidth());
        assertEquals(800, settings.windowHeight());
        assertEquals(GraphicsPreset.MEDIUM, settings.graphicsPreset());
        assertEquals(1f, settings.uiScale());
    }

    @Test
    void invalidDisplayValuesAreClampedAndNormalized() {
        GameSettings settings = new GameSettings(
            true,
            0.16f,
            60,
            null,
            100,
            100,
            Float.NaN,
            null,
            false
        );

        assertEquals(WindowMode.WINDOWED, settings.windowMode());
        assertEquals(GameSettings.MIN_WINDOW_WIDTH, settings.windowWidth());
        assertEquals(GameSettings.MIN_WINDOW_HEIGHT, settings.windowHeight());
        assertEquals(GameSettings.DEFAULT_UI_SCALE, settings.uiScale());
        assertEquals(GraphicsPreset.MEDIUM, settings.graphicsPreset());
    }

    @Test
    void resolutionAndPresetCycleDeterministically() {
        GameSettings settings = GameSettings.defaults()
            .withResolution(DisplayResolution.FULL_HD)
            .withGraphicsPreset(GraphicsPreset.HIGH);

        assertEquals(DisplayResolution.FULL_HD, settings.displayResolution());
        assertEquals(GraphicsPreset.HIGH, settings.graphicsPreset());
        assertEquals(DisplayResolution.HD_720, settings.displayResolution().shifted(1));
        assertEquals(GraphicsPreset.LOW, settings.graphicsPreset().shifted(1));
    }
}
