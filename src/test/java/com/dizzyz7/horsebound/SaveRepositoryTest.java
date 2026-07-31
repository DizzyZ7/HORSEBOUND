// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsCompleteWorldState() {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        SaveGame expected = sampleSave(17, 9, 72f);

        repository.save("slot-1", expected);
        SaveGame actual = repository.load("slot-1").orElseThrow();

        assertEquals(expected, actual);
        assertTrue(repository.exists("slot-1"));
    }

    @Test
    void firstSuccessfulSaveAlreadyHasRecoverableBackup() throws Exception {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        SaveGame expected = sampleSave(7, 3, 48f);

        repository.save("slot-1", expected);
        Path primary = repository.root().resolve("saves").resolve("slot-1").resolve("save.hbs");
        Path backup = repository.root().resolve("saves").resolve("slot-1").resolve("save.bak");

        assertTrue(Files.isRegularFile(backup));
        Files.writeString(primary, "corrupted");

        assertEquals(expected, repository.load("slot-1").orElseThrow());
    }

    @Test
    void fallsBackToPreviousBackupWhenLatestPrimaryIsCorrupt() throws Exception {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        SaveGame first = sampleSave(4, 5, 20f);
        SaveGame second = sampleSave(99, 1, 100f);

        repository.save("slot-1", first);
        repository.save("slot-1", second);

        Path primary = repository.root().resolve("saves").resolve("slot-1").resolve("save.hbs");
        Files.writeString(primary, "corrupted");

        SaveGame recovered = repository.load("slot-1").orElseThrow();
        assertEquals(first, recovered);
    }

    @Test
    void saveAfterRecoveryDoesNotReplaceGoodBackupWithCorruptPrimary() throws Exception {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        SaveGame first = sampleSave(4, 5, 20f);
        SaveGame second = sampleSave(99, 1, 100f);
        SaveGame third = sampleSave(12, 6, 81f);

        repository.save("slot-1", first);
        repository.save("slot-1", second);

        Path primary = repository.root().resolve("saves").resolve("slot-1").resolve("save.hbs");
        Files.writeString(primary, "corrupted-second-primary");
        assertEquals(first, repository.load("slot-1").orElseThrow());

        repository.save("slot-1", third);
        Files.writeString(primary, "corrupted-third-primary");

        assertEquals(first, repository.load("slot-1").orElseThrow());
    }

    private static SaveGame sampleSave(int wood, int apples, float trust) {
        UUID horseId = UUID.fromString("6f50e536-4459-4be6-9cee-5839aef4b59c");
        return new SaveGame(
            SaveGame.CURRENT_VERSION,
            4242424242L,
            1722456000000L,
            0.51f,
            new SaveGame.PlayerData(11.5f, -7.25f, 135f, wood, apples),
            new SaveGame.PushikData(9.5f, -6.0f, 42f),
            List.of(new SaveGame.HorseData(horseId, "Ember", 18f, 8f, 25f, trust, 83f, true)),
            List.of(new SaveGame.FenceData(4f, 6f, 90f)),
            List.of(2, 7, 19)
        );
    }
}
