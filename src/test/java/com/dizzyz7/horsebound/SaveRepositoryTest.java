// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveRepositoryTest {
    private static final int MAGIC = 0x48425356;

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
    void replacingARanchAlsoReplacesItsRecoveryBackup() throws Exception {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        SaveGame oldRanch = sampleSave(4, 5, 20f);
        SaveGame replacement = new SaveGame(
            SaveGame.CURRENT_VERSION,
            999999L,
            oldRanch.savedAtEpochMillis() + 1L,
            oldRanch.worldTime(),
            oldRanch.player(),
            oldRanch.pushik(),
            oldRanch.horses(),
            oldRanch.fences(),
            oldRanch.structures(),
            oldRanch.hotbar(),
            oldRanch.harvestedTreeIds()
        );

        repository.save("slot-1", oldRanch);
        repository.replace("slot-1", replacement);

        Path primary = repository.root().resolve("saves").resolve("slot-1").resolve("save.hbs");
        Files.writeString(primary, "corrupted-replacement-primary");

        assertEquals(replacement, repository.load("slot-1").orElseThrow());
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

    @Test
    void versionOneSaveMigratesWithoutLosingHorseProgress() throws Exception {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        UUID horseId = UUID.fromString("6f50e536-4459-4be6-9cee-5839aef4b59c");
        Path primary = createSlot(repository, "slot-1");

        try (DataOutputStream out = output(primary)) {
            writeHeader(out, 1, 123456789L, 0.51f);
            writeLegacyPlayer(out, 17, 9);
            writeLegacyPushik(out);

            out.writeInt(1);
            writeVersionOneHorse(out, horseId);
            out.writeInt(0);
            out.writeInt(0);
        }

        SaveGame migrated = repository.load("slot-1").orElseThrow();
        SaveGame.HorseData horse = migrated.horses().getFirst();

        assertEquals(SaveGame.CURRENT_VERSION, migrated.saveVersion());
        assertEquals(123456789L, migrated.worldSeed());
        assertEquals(horseId, horse.id());
        assertEquals("Ember", horse.name());
        assertEquals(72f, horse.trust());
        assertEquals(83f, horse.stamina());
        assertTrue(horse.tamed());
        assertEquals(HorsePersonality.fromIdentity(horseId), horse.personality());
        assertTrue(horse.bond() >= 20f);
        assertTrue(horse.fear() <= 12f);
        assertEquals(17, inventoryAmount(migrated, ItemId.WOOD));
        assertEquals(9, inventoryAmount(migrated, ItemId.APPLE));
        assertEquals(45f, migrated.pushik().affection());
        assertEquals(PushikState.FOLLOW, migrated.pushik().state());
    }

    @Test
    void versionTwoSaveMigratesToTypedInventoryAndPersistentPushikDefaults() throws Exception {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        UUID horseId = UUID.fromString("6f50e536-4459-4be6-9cee-5839aef4b59c");
        Path primary = createSlot(repository, "slot-2");

        try (DataOutputStream out = output(primary)) {
            writeHeader(out, 2, 987654321L, 0.73f);
            writeLegacyPlayer(out, 31, 6);
            writeLegacyPushik(out);

            out.writeInt(1);
            writeVersionTwoHorse(out, horseId);
            out.writeInt(1);
            out.writeFloat(4f);
            out.writeFloat(6f);
            out.writeFloat(90f);
            out.writeInt(2);
            out.writeInt(7);
            out.writeInt(19);
        }

        SaveGame migrated = repository.load("slot-2").orElseThrow();

        assertEquals(SaveGame.CURRENT_VERSION, migrated.saveVersion());
        assertEquals(31, inventoryAmount(migrated, ItemId.WOOD));
        assertEquals(6, inventoryAmount(migrated, ItemId.APPLE));
        assertEquals(45f, migrated.pushik().affection());
        assertEquals(PushikState.FOLLOW, migrated.pushik().state());
        assertEquals(HorsePersonality.CURIOUS, migrated.horses().getFirst().personality());
        assertEquals(66f, migrated.horses().getFirst().bond());
        assertEquals(7f, migrated.horses().getFirst().fear());

        repository.save("slot-2", migrated);
        SaveGame rewrittenAsV3 = repository.load("slot-2").orElseThrow();
        assertEquals(migrated, rewrittenAsV3);
    }

    private static SaveGame sampleSave(int wood, int apples, float trust) {
        UUID horseId = UUID.fromString("6f50e536-4459-4be6-9cee-5839aef4b59c");
        return new SaveGame(
            SaveGame.CURRENT_VERSION,
            4242424242L,
            1722456000000L,
            0.51f,
            new SaveGame.PlayerData(
                11.5f,
                -7.25f,
                135f,
                wood,
                apples,
                List.of(
                    new SaveGame.ItemStackData(ItemId.WOOD.name(), wood),
                    new SaveGame.ItemStackData(ItemId.APPLE.name(), apples)
                )
            ),
            new SaveGame.PushikData(9.5f, -6.0f, 42f, 88f, PushikState.SIT),
            List.of(new SaveGame.HorseData(
                horseId,
                "Ember",
                18f,
                8f,
                25f,
                trust,
                83f,
                true,
                HorsePersonality.CURIOUS,
                66f,
                7f
            )),
            List.of(new SaveGame.FenceData(4f, 6f, 90f)),
            List.of(2, 7, 19)
        );
    }

    private static Path createSlot(SaveRepository repository, String slot) throws Exception {
        Path directory = repository.root().resolve("saves").resolve(slot);
        Files.createDirectories(directory);
        return directory.resolve("save.hbs");
    }

    private static DataOutputStream output(Path primary) throws Exception {
        return new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(primary)));
    }

    private static void writeHeader(DataOutputStream out, int version, long seed, float worldTime) throws Exception {
        out.writeInt(MAGIC);
        out.writeInt(version);
        out.writeLong(seed);
        out.writeLong(1722456000000L);
        out.writeFloat(worldTime);
    }

    private static void writeLegacyPlayer(DataOutputStream out, int wood, int apples) throws Exception {
        out.writeFloat(11.5f);
        out.writeFloat(-7.25f);
        out.writeFloat(135f);
        out.writeInt(wood);
        out.writeInt(apples);
    }

    private static void writeLegacyPushik(DataOutputStream out) throws Exception {
        out.writeFloat(9.5f);
        out.writeFloat(-6f);
        out.writeFloat(42f);
    }

    private static void writeVersionOneHorse(DataOutputStream out, UUID horseId) throws Exception {
        out.writeLong(horseId.getMostSignificantBits());
        out.writeLong(horseId.getLeastSignificantBits());
        out.writeUTF("Ember");
        out.writeFloat(18f);
        out.writeFloat(8f);
        out.writeFloat(25f);
        out.writeFloat(72f);
        out.writeFloat(83f);
        out.writeBoolean(true);
    }

    private static void writeVersionTwoHorse(DataOutputStream out, UUID horseId) throws Exception {
        writeVersionOneHorse(out, horseId);
        out.writeUTF(HorsePersonality.CURIOUS.name());
        out.writeFloat(66f);
        out.writeFloat(7f);
    }

    private static int inventoryAmount(SaveGame save, ItemId itemId) {
        return save.player().inventoryItems().stream()
            .filter(item -> item.itemId().equals(itemId.name()))
            .mapToInt(SaveGame.ItemStackData::amount)
            .sum();
    }
}
