// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {
    @Test
    void totalAmountIsNotLimitedByOneStack() {
        Inventory inventory = Inventory.starter(4, 5);
        assertEquals(4, inventory.count(ItemId.WOOD));
        assertEquals(5, inventory.count(ItemId.APPLE));
        assertEquals(6, inventory.count(ItemId.HAY));
        assertEquals(2, inventory.count(ItemId.WATER_BUCKET));

        assertTrue(inventory.remove(ItemId.WOOD, 2));
        assertEquals(2, inventory.count(ItemId.WOOD));
        assertFalse(inventory.remove(ItemId.WOOD, 3));

        assertEquals(100, inventory.add(ItemId.APPLE, 100));
        assertEquals(105, inventory.count(ItemId.APPLE));
    }

    @Test
    void stackViewSplitsAggregateCountsWithoutLosingItems() {
        Inventory inventory = new Inventory();
        inventory.add(ItemId.APPLE, 45);

        List<InventoryStack> stacks = inventory.stackView();

        assertEquals(List.of(
            new InventoryStack(ItemId.APPLE, 20),
            new InventoryStack(ItemId.APPLE, 20),
            new InventoryStack(ItemId.APPLE, 5)
        ), stacks);
        assertEquals(45, stacks.stream().mapToInt(InventoryStack::amount).sum());
    }

    @Test
    void restoredDuplicateEntriesAreSummedAndUnknownItemsIgnored() {
        Inventory inventory = Inventory.restore(
            List.of(
                new SaveGame.ItemStackData("WOOD", 90),
                new SaveGame.ItemStackData("WOOD", 30),
                new SaveGame.ItemStackData("FUTURE_RESOURCE", 999)
            ),
            1,
            1
        );

        assertEquals(120, inventory.count(ItemId.WOOD));
        assertEquals(0, inventory.count(ItemId.APPLE));
    }
}
