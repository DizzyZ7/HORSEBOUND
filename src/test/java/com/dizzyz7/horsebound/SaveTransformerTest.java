// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaveTransformerTest {
    @TempDir
    Path tempDir;

    @Test
    void liveTransformerEnrichesEverySave() {
        SaveService service = new SaveService(new SaveRepository(tempDir.resolve("HORSEBOUND")));
        SaveGame base = service.createNewWorld("slot-1");
        Object owner = new Object();
        UUID feederId = UUID.fromString("ef13acd1-4982-47e7-908d-dc92db754fd2");

        service.setSaveTransformer(owner, save -> new SaveGame(
            SaveGame.CURRENT_VERSION,
            save.worldSeed(),
            save.savedAtEpochMillis(),
            save.worldTime(),
            save.player(),
            save.pushik(),
            save.horses(),
            save.fences(),
            List.of(new SaveGame.StructureData(
                feederId,
                HomesteadStructureType.FEEDER,
                2f,
                3f,
                0f,
                9
            )),
            new SaveGame.HotbarData(1, List.of("WOOD", "HAY", "", "", "", "", "", "")),
            save.harvestedTreeIds()
        ));

        service.save(base);
        SaveGame actual = service.loadWorld("slot-1");

        assertEquals(feederId, actual.structures().getFirst().id());
        assertEquals(9, actual.structures().getFirst().storedUnits());
        assertEquals(1, actual.hotbar().selectedIndex());
    }

    @Test
    void staleOwnerCannotClearNewSessionTransformer() {
        SaveService service = new SaveService(new SaveRepository(tempDir.resolve("HORSEBOUND")));
        SaveGame base = service.createNewWorld("slot-2");
        Object oldOwner = new Object();
        Object newOwner = new Object();

        service.setSaveTransformer(oldOwner, save -> save);
        service.setSaveTransformer(newOwner, save -> new SaveGame(
            save.saveVersion(),
            save.worldSeed(),
            9999L,
            save.worldTime(),
            save.player(),
            save.pushik(),
            save.horses(),
            save.fences(),
            save.structures(),
            save.hotbar(),
            save.harvestedTreeIds()
        ));
        service.clearSaveTransformer(oldOwner);
        service.save(base);

        assertEquals(9999L, service.loadWorld("slot-2").savedAtEpochMillis());
    }
}
