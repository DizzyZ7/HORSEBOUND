// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void missingSettingsReturnDefaults() {
        SettingsRepository repository = new SettingsRepository(tempDir.resolve("settings.properties"));
        assertEquals(GameSettings.defaults(), repository.load());
    }

    @Test
    void extendedSettingsRoundTrip() {
        SettingsRepository repository = new SettingsRepository(tempDir.resolve("settings.properties"));
        GameSettings expected = new GameSettings(
            false,
            0.23f,
            150,
            WindowMode.FULLSCREEN,
            1920,
            1080,
            1.30f,
            GraphicsPreset.HIGH,
            true,
            0.35f,
            0.25f
        );

        repository.save(expected);

        assertEquals(expected, repository.load());
    }

    @Test
    void legacySettingsReceiveDeckSafeDisplayAndAudioDefaults() throws Exception {
        Path path = tempDir.resolve("settings.properties");
        Files.writeString(path, "vsync=false\nmouseSensitivity=0.20\nautosaveSeconds=120\n");
        SettingsRepository repository = new SettingsRepository(path);

        assertEquals(new GameSettings(false, 0.20f, 120), repository.load());
        assertEquals(GameSettings.DEFAULT_SFX_VOLUME, repository.load().sfxVolume());
        assertEquals(GameSettings.DEFAULT_AMBIENCE_VOLUME, repository.load().ambienceVolume());
    }

    @Test
    void corruptSettingsFallBackSafely() throws Exception {
        Path path = tempDir.resolve("settings.properties");
        Files.writeString(
            path,
            "mouseSensitivity=not-a-number\n"
                + "autosaveSeconds=broken\n"
                + "vsync=false\n"
                + "windowMode=spaceship\n"
                + "windowWidth=nope\n"
                + "uiScale=NaN\n"
                + "graphicsPreset=cinematic\n"
                + "sfxVolume=broken\n"
                + "ambienceVolume=broken\n"
        );
        SettingsRepository repository = new SettingsRepository(path);

        assertEquals(new GameSettings(false, 0.16f, 60), repository.load());
    }

    @Test
    void audioBusVolumesAreClampedOnLoad() throws Exception {
        Path path = tempDir.resolve("settings.properties");
        Files.writeString(path, "sfxVolume=5.0\nambienceVolume=-3.0\n");

        GameSettings loaded = new SettingsRepository(path).load();
        assertEquals(1f, loaded.sfxVolume());
        assertEquals(0f, loaded.ambienceVolume());
    }
}
