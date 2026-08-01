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

class SaveVersionFourTest {
    private static final int MAGIC = 0x48425356;

    @TempDir
    Path tempDir;

    @Test
    void versionFourRoundTripsHomesteadHotbarAndHorseNeeds() {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        UUID horseId = UUID.fromString("6f50e536-4459-4be6-9cee-5839aef4b59c");
        UUID feederId = UUID.fromString("ef13acd1-4982-47e7-908d-dc92db754fd2");
        SaveGame expected = new SaveGame(
            SaveGame.CURRENT_VERSION,
            424242L,
            1722456000000L,
            0.42f,
            new SaveGame.PlayerData(
                2f,
                3f,
                90f,
                150,
                7,
                List.of(
                    new SaveGame.ItemStackData(ItemId.WOOD.name(), 150),
                    new SaveGame.ItemStackData(ItemId.HAY.name(), 24),
                    new SaveGame.ItemStackData(ItemId.WATER_BUCKET.name(), 8)
                )
            ),
            new SaveGame.PushikData(4f, 5f, 20f, 91f, PushikState.GREET),
            List.of(new SaveGame.HorseData(
                horseId,
                "Ember",
                10f,
                11f,
                40f,
                88f,
                73f,
                true,
                HorsePersonality.CURIOUS,
                79f,
                4f,
                63f,
                54f,
                47f
            )),
            List.of(),
            List.of(new SaveGame.StructureData(
                feederId,
                HomesteadStructureType.FEEDER,
                8f,
                9f,
                180f,
                17
            )),
            new SaveGame.HotbarData(
                2,
                List.of("HAY", "WATER_BUCKET", "APPLE", "WOOD", "", "", "", "")
            ),
            List.of(3, 8, 13)
        );

        repository.save("slot-1", expected);
        SaveGame actual = repository.load("slot-1").orElseThrow();

        assertEquals(expected, actual);
        assertEquals(150, actual.player().inventoryItems().stream()
            .filter(item -> item.itemId().equals(ItemId.WOOD.name()))
            .mapToInt(SaveGame.ItemStackData::amount)
            .sum());
    }

    @Test
    void realVersionThreeBinaryMigratesToV4WithoutTruncatingInventory() throws Exception {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        Path directory = repository.root().resolve("saves").resolve("slot-2");
        Files.createDirectories(directory);
        Path primary = directory.resolve("save.hbs");
        UUID horseId = UUID.fromString("6f50e536-4459-4be6-9cee-5839aef4b59c");

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(primary)))) {
            out.writeInt(MAGIC);
            out.writeInt(3);
            out.writeLong(987654321L);
            out.writeLong(1722456000000L);
            out.writeFloat(0.61f);

            out.writeFloat(1f);
            out.writeFloat(2f);
            out.writeFloat(45f);
            out.writeInt(150);
            out.writeInt(6);
            out.writeInt(3);
            out.writeUTF(ItemId.WOOD.name());
            out.writeInt(150);
            out.writeUTF(ItemId.APPLE.name());
            out.writeInt(6);
            out.writeUTF(ItemId.HAY.name());
            out.writeInt(12);

            out.writeFloat(3f);
            out.writeFloat(4f);
            out.writeFloat(30f);
            out.writeFloat(77f);
            out.writeUTF(PushikState.SIT.name());

            out.writeInt(1);
            out.writeLong(horseId.getMostSignificantBits());
            out.writeLong(horseId.getLeastSignificantBits());
            out.writeUTF("Ember");
            out.writeFloat(5f);
            out.writeFloat(6f);
            out.writeFloat(70f);
            out.writeFloat(80f);
            out.writeFloat(90f);
            out.writeBoolean(true);
            out.writeUTF(HorsePersonality.CURIOUS.name());
            out.writeFloat(66f);
            out.writeFloat(7f);

            out.writeInt(1);
            out.writeFloat(8f);
            out.writeFloat(9f);
            out.writeFloat(90f);

            out.writeInt(2);
            out.writeInt(7);
            out.writeInt(19);
        }

        SaveGame migrated = repository.load("slot-2").orElseThrow();

        assertEquals(SaveGame.CURRENT_VERSION, migrated.saveVersion());
        assertEquals(150, migrated.player().inventoryItems().stream()
            .filter(item -> item.itemId().equals(ItemId.WOOD.name()))
            .mapToInt(SaveGame.ItemStackData::amount)
            .sum());
        assertEquals(HorseNeeds.healthy(), migrated.horses().getFirst().needs());
        assertEquals(1, migrated.structures().size());
        assertEquals(HomesteadStructureType.FENCE, migrated.structures().getFirst().type());
        assertEquals(Hotbar.SLOT_COUNT, migrated.hotbar().itemIds().size());
        assertEquals(ItemId.WOOD.name(), migrated.hotbar().itemIds().getFirst());

        repository.save("slot-2", migrated);
        assertEquals(migrated, repository.load("slot-2").orElseThrow());
        assertTrue(Files.isRegularFile(directory.resolve("save.bak")));
    }
}
