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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveVersionFiveTest {
    private static final int MAGIC = 0x48425356;

    @TempDir
    Path tempDir;

    @Test
    void roundTripsOpenGateAndChestContents() {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        SaveGame base = SaveGame.fresh(new WorldSeed(555L));
        SaveGame expected = new SaveGame(
            SaveGame.CURRENT_VERSION,
            base.worldSeed(),
            base.savedAtEpochMillis(),
            base.worldTime(),
            base.player(),
            base.pushik(),
            base.horses(),
            base.fences(),
            List.of(
                new SaveGame.StructureData(
                    UUID.fromString("4fe56cee-5078-4f88-b122-b3d80d2367e1"),
                    HomesteadStructureType.GATE,
                    3f,
                    4f,
                    90f,
                    0,
                    true,
                    List.of()
                ),
                new SaveGame.StructureData(
                    UUID.fromString("31c4c1d5-ff13-43db-b13b-1d94bf642a87"),
                    HomesteadStructureType.CHEST,
                    6f,
                    7f,
                    15f,
                    0,
                    false,
                    List.of(
                        new SaveGame.ItemStackData(ItemId.APPLE.name(), 9),
                        new SaveGame.ItemStackData(ItemId.WOOD.name(), 42)
                    )
                )
            ),
            base.hotbar(),
            base.harvestedTreeIds()
        );

        repository.save("slot-1", expected);
        SaveGame actual = repository.load("slot-1").orElseThrow();

        assertEquals(expected, actual);
        assertTrue(actual.structures().getFirst().open());
        assertEquals(2, actual.structures().get(1).storedItems().size());
    }

    @Test
    void realVersionFourBinaryMigratesWithClosedGateAndEmptyChest() throws Exception {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        Path directory = repository.root().resolve("saves").resolve("slot-2");
        Files.createDirectories(directory);
        Path primary = directory.resolve("save.hbs");
        UUID gateId = UUID.fromString("4fe56cee-5078-4f88-b122-b3d80d2367e1");
        UUID chestId = UUID.fromString("31c4c1d5-ff13-43db-b13b-1d94bf642a87");

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(primary)))) {
            out.writeInt(MAGIC);
            out.writeInt(4);
            out.writeLong(123L);
            out.writeLong(456L);
            out.writeFloat(0.5f);

            out.writeFloat(1f);
            out.writeFloat(2f);
            out.writeFloat(3f);
            out.writeInt(4);
            out.writeInt(5);
            out.writeInt(2);
            out.writeUTF(ItemId.WOOD.name());
            out.writeInt(4);
            out.writeUTF(ItemId.APPLE.name());
            out.writeInt(5);

            out.writeFloat(2f);
            out.writeFloat(3f);
            out.writeFloat(4f);
            out.writeFloat(45f);
            out.writeUTF(PushikState.FOLLOW.name());

            out.writeInt(0);
            out.writeInt(0);
            out.writeInt(0);

            out.writeInt(2);
            writeV4Structure(out, gateId, HomesteadStructureType.GATE, 5f, 6f, 90f, 0);
            writeV4Structure(out, chestId, HomesteadStructureType.CHEST, 7f, 8f, 0f, 0);

            out.writeInt(0);
            out.writeInt(8);
            for (int i = 0; i < 8; i++) out.writeUTF("");
        }

        SaveGame migrated = repository.load("slot-2").orElseThrow();

        assertEquals(SaveGame.CURRENT_VERSION, migrated.saveVersion());
        assertFalse(migrated.structures().getFirst().open());
        assertTrue(migrated.structures().get(1).storedItems().isEmpty());

        repository.save("slot-2", migrated);
        assertEquals(migrated, repository.load("slot-2").orElseThrow());
    }

    private static void writeV4Structure(
        DataOutputStream out,
        UUID id,
        HomesteadStructureType type,
        float x,
        float z,
        float heading,
        int storedUnits
    ) throws Exception {
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
        out.writeUTF(type.name());
        out.writeFloat(x);
        out.writeFloat(z);
        out.writeFloat(heading);
        out.writeInt(storedUnits);
    }
}
