// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HotbarTest {
    @Test
    void defaultsProvideEightSlotsAndWrapSelection() {
        Hotbar hotbar = Hotbar.defaults();

        assertEquals(Hotbar.SLOT_COUNT, hotbar.slots().size());
        assertEquals(ItemId.WOOD, hotbar.selectedItem());
        assertNull(hotbar.itemAt(7));

        hotbar.cycle(-1);
        assertEquals(7, hotbar.selectedIndex());
        hotbar.cycle(2);
        assertEquals(1, hotbar.selectedIndex());
        assertEquals(ItemId.HAY, hotbar.selectedItem());
    }

    @Test
    void saveDataRoundTripsUnknownAndEmptySlotsSafely() {
        Hotbar hotbar = Hotbar.defaults();
        hotbar.assign(0, ItemId.WATER_BUCKET);
        hotbar.assign(1, null);
        hotbar.select(6);

        Hotbar restored = Hotbar.restore(hotbar.toSaveData());

        assertEquals(6, restored.selectedIndex());
        assertEquals(ItemId.WATER_BUCKET, restored.itemAt(0));
        assertNull(restored.itemAt(1));
        assertEquals(Hotbar.SLOT_COUNT, restored.slots().size());
    }
}
