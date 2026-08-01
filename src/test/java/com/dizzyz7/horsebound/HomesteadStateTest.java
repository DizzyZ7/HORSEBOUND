// HORSEBOUND — Created by Dimash Janibekov (DizZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomesteadStateTest {
    @Test
    void placementConsumesCostAndFailedPlacementIsAtomic() {
        Inventory inventory = new Inventory();
        inventory.add(ItemId.WOOD, 10);
        HomesteadState homestead = new HomesteadState();

        Optional<PlacedStructure> feeder = homestead.place(
            HomesteadStructureType.FEEDER,
            3f,
            4f,
            90f,
            inventory
        );

        assertTrue(feeder.isPresent());
        assertEquals(4, inventory.count(ItemId.WOOD));
        assertEquals(1, homestead.structures().size());

        assertTrue(homestead.place(HomesteadStructureType.GATE, 8f, 2f, 0f, inventory).isPresent());
        assertEquals(0, inventory.count(ItemId.WOOD));

        assertFalse(homestead.place(HomesteadStructureType.FENCE, 9f, 2f, 0f, inventory).isPresent());
        assertEquals(0, inventory.count(ItemId.WOOD));
        assertEquals(2, homestead.structures().size());
    }

    @Test
    void feederAcceptsInventoryResourceAndServicesNearbyHorse() {
        Inventory inventory = new Inventory();
        inventory.add(ItemId.WOOD, 6);
        inventory.add(ItemId.HAY, 2);
        HomesteadState homestead = new HomesteadState();
        PlacedStructure feeder = homestead.place(
            HomesteadStructureType.FEEDER,
            10f,
            10f,
            0f,
            inventory
        ).orElseThrow();

        assertEquals(5, homestead.depositOneResource(feeder.id(), inventory));
        assertEquals(1, inventory.count(ItemId.HAY));
        assertEquals(5, feeder.storedUnits());

        assertTrue(homestead.consumeNearestResource(ItemId.HAY, 12f, 10f, 7.5f));
        assertEquals(4, feeder.storedUnits());
        assertFalse(homestead.consumeNearestResource(ItemId.HAY, 100f, 100f, 7.5f));
    }

    @Test
    void restoredStatePreservesIdentityTypeAndStoredUnits() {
        Inventory inventory = new Inventory();
        inventory.add(ItemId.WOOD, 6);
        inventory.add(ItemId.HAY, 1);
        HomesteadState original = new HomesteadState();
        PlacedStructure feeder = original.place(
            HomesteadStructureType.FEEDER,
            -5f,
            7f,
            30f,
            inventory
        ).orElseThrow();
        original.depositOneResource(feeder.id(), inventory);

        HomesteadState restored = HomesteadState.restore(original.toSaveData());
        PlacedStructure actual = restored.find(feeder.id()).orElseThrow();

        assertEquals(HomesteadStructureType.FEEDER, actual.type());
        assertEquals(5, actual.storedUnits());
        assertEquals(-5f, actual.x());
        assertEquals(7f, actual.z());
    }
}
