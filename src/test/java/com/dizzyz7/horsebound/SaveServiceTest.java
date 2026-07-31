// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void exposesThreeEmptyRanchSlots() {
        SaveService service = new SaveService(new SaveRepository(tempDir.resolve("HORSEBOUND")));

        List<SaveSlotInfo> slots = service.listSlots();

        assertEquals(3, slots.size());
        assertTrue(slots.stream().allMatch(slot -> slot.state() == SaveSlotInfo.State.EMPTY));
        assertFalse(service.hasContinue());
    }

    @Test
    void newWorldActivatesRequestedSlotAndAppearsInMetadata() {
        SaveService service = new SaveService(new SaveRepository(tempDir.resolve("HORSEBOUND")));

        SaveGame created = service.createNewWorld("slot-2");
        SaveSlotInfo slot = service.listSlots().get(1);

        assertEquals("slot-2", service.activeSlot());
        assertEquals(SaveSlotInfo.State.READY, slot.state());
        assertEquals(created.worldSeed(), slot.worldSeed());
        assertTrue(service.hasContinue());
    }

    @Test
    void mostRecentReadySlotUsesSavedTimestamp() {
        SaveRepository repository = new SaveRepository(tempDir.resolve("HORSEBOUND"));
        repository.save("slot-1", withSavedAt(SaveGame.fresh(new WorldSeed(11L)), 1000L));
        repository.save("slot-3", withSavedAt(SaveGame.fresh(new WorldSeed(33L)), 3000L));
        SaveService service = new SaveService(repository);

        SaveSlotInfo mostRecent = service.mostRecentReadySlot().orElseThrow();

        assertEquals("slot-3", mostRecent.slotId());
        assertEquals(33L, mostRecent.worldSeed());
    }

    private static SaveGame withSavedAt(SaveGame source, long savedAt) {
        return new SaveGame(
            source.saveVersion(),
            source.worldSeed(),
            savedAt,
            source.worldTime(),
            source.player(),
            source.pushik(),
            source.horses(),
            source.fences(),
            source.harvestedTreeIds()
        );
    }
}
