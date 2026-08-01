// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomesteadOperationsTest {
    @Test
    void gateOpenStateAndRelocationArePersistentDomainOperations() {
        Inventory inventory = new Inventory();
        inventory.add(ItemId.WOOD, 20);
        HomesteadState homestead = new HomesteadState();
        PlacedStructure gate = homestead.place(HomesteadStructureType.GATE, 1f, 2f, 0f, inventory).orElseThrow();

        assertFalse(gate.isOpen());
        assertTrue(homestead.toggleGate(gate.id()));
        assertTrue(gate.isOpen());
        assertTrue(homestead.relocate(gate.id(), 8f, 9f, 450f));
        assertEquals(8f, gate.x());
        assertEquals(9f, gate.z());
        assertEquals(90f, gate.heading());

        HomesteadState restored = HomesteadState.restore(homestead.toSaveData());
        PlacedStructure restoredGate = restored.find(gate.id()).orElseThrow();
        assertTrue(restoredGate.isOpen());
        assertEquals(8f, restoredGate.x());
    }

    @Test
    void chestTransfersOneItemInEitherDirection() {
        Inventory inventory = new Inventory();
        inventory.add(ItemId.WOOD, 20);
        inventory.add(ItemId.APPLE, 3);
        HomesteadState homestead = new HomesteadState();
        PlacedStructure chest = homestead.place(HomesteadStructureType.CHEST, 0f, 0f, 0f, inventory).orElseThrow();

        assertEquals(
            HomesteadState.TransferResult.SUCCESS,
            homestead.storeOneItem(chest.id(), inventory, ItemId.APPLE)
        );
        assertEquals(2, inventory.count(ItemId.APPLE));
        assertEquals(1, chest.itemStorage().count(ItemId.APPLE));

        assertEquals(
            HomesteadState.TransferResult.SUCCESS,
            homestead.takeOneItem(chest.id(), inventory, ItemId.APPLE)
        );
        assertEquals(3, inventory.count(ItemId.APPLE));
        assertTrue(chest.itemStorage().isEmpty());
    }

    @Test
    void dismantleRefusesNonEmptyStorageAndFullRefundDestination() {
        Inventory inventory = new Inventory();
        inventory.add(ItemId.WOOD, 20);
        inventory.add(ItemId.APPLE, 1);
        HomesteadState homestead = new HomesteadState();
        PlacedStructure chest = homestead.place(HomesteadStructureType.CHEST, 0f, 0f, 0f, inventory).orElseThrow();
        homestead.storeOneItem(chest.id(), inventory, ItemId.APPLE);

        assertEquals(
            HomesteadState.DismantleResult.STORAGE_NOT_EMPTY,
            homestead.dismantle(chest.id(), inventory)
        );
        assertTrue(homestead.find(chest.id()).isPresent());

        homestead.takeOneItem(chest.id(), inventory, ItemId.APPLE);
        Inventory full = new Inventory(1);
        full.add(ItemId.APPLE, ItemId.APPLE.stackLimit());
        assertEquals(
            HomesteadState.DismantleResult.INVENTORY_FULL,
            homestead.dismantle(chest.id(), full)
        );
        assertTrue(homestead.find(chest.id()).isPresent());
    }

    @Test
    void missingStructureOperationsAreSafe() {
        HomesteadState homestead = new HomesteadState();
        UUID missing = UUID.randomUUID();
        assertFalse(homestead.toggleGate(missing));
        assertFalse(homestead.relocate(missing, 1f, 2f, 3f));
        assertEquals(
            HomesteadState.DismantleResult.NOT_FOUND,
            homestead.dismantle(missing, new Inventory())
        );
    }
}
