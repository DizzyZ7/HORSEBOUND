// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {
    @Test
    void addsRemovesAndRespectsStackLimits() {
        Inventory inventory = Inventory.starter(4, 5);
        assertEquals(4, inventory.count(ItemId.WOOD));
        assertEquals(5, inventory.count(ItemId.APPLE));

        assertTrue(inventory.remove(ItemId.WOOD, 2));
        assertEquals(2, inventory.count(ItemId.WOOD));
        assertFalse(inventory.remove(ItemId.WOOD, 3));
        assertEquals(2, inventory.count(ItemId.WOOD));

        int accepted = inventory.add(ItemId.APPLE, 100);
        assertEquals(15, accepted);
        assertEquals(ItemId.APPLE.stackLimit(), inventory.count(ItemId.APPLE));
    }
}
