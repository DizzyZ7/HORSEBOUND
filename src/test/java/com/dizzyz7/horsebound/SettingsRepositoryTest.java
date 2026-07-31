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
    void settingsRoundTrip() {
        SettingsRepository repository = new SettingsRepository(tempDir.resolve("settings.properties"));
        GameSettings expected = new GameSettings(false, 0.23f, 150);

        repository.save(expected);

        assertEquals(expected, repository.load());
    }

    @Test
    void corruptSettingsFallBackSafely() throws Exception {
        Path path = tempDir.resolve("settings.properties");
        Files.writeString(path, "mouseSensitivity=not-a-number\nautosaveSeconds=broken\nvsync=false\n");
        SettingsRepository repository = new SettingsRepository(path);

        assertEquals(new GameSettings(false, 0.16f, 60), repository.load());
    }
}
