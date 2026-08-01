// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTransferTest {
    @Test
    void transferIsAtomicWhenDestinationIsFull() {
        Inventory source = new Inventory();
        source.add(ItemId.WOOD, 10);
        Inventory destination = new Inventory(1);
        destination.add(ItemId.APPLE, ItemId.APPLE.stackLimit());

        assertFalse(source.transferTo(destination, ItemId.WOOD, 1));
        assertEquals(10, source.count(ItemId.WOOD));
        assertEquals(0, destination.count(ItemId.WOOD));
    }

    @Test
    void canAcceptSimulatesSeveralItemTypesWithoutMutating() {
        Inventory inventory = new Inventory(2);
        inventory.add(ItemId.WOOD, 90);

        assertTrue(inventory.canAccept(List.of(
            new SaveGame.ItemStackData(ItemId.WOOD.name(), 9),
            new SaveGame.ItemStackData(ItemId.APPLE.name(), 20)
        )));
        assertFalse(inventory.canAccept(List.of(
            new SaveGame.ItemStackData(ItemId.WOOD.name(), 10),
            new SaveGame.ItemStackData(ItemId.APPLE.name(), 20)
        )));
        assertEquals(90, inventory.count(ItemId.WOOD));
        assertEquals(0, inventory.count(ItemId.APPLE));
    }

    @Test
    void transferAllNeverPartiallyMovesContents() {
        Inventory chest = new Inventory(2);
        chest.add(ItemId.WOOD, 50);
        chest.add(ItemId.APPLE, 20);
        Inventory player = new Inventory(1);

        assertFalse(chest.transferAllTo(player));
        assertEquals(50, chest.count(ItemId.WOOD));
        assertEquals(20, chest.count(ItemId.APPLE));
        assertTrue(player.isEmpty());
    }
}
