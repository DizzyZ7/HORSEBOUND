// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchUndoManagerTest {
    @Test
    void undoPlacementRemovesUnchangedStructureAndReturnsFullRecipe() {
        Inventory inventory = funded(HomesteadStructureType.FENCE);
        Map<ItemId, Integer> before = inventory.snapshot();
        HomesteadState state = new HomesteadState();
        PlacedStructure placed = state.place(HomesteadStructureType.FENCE, 4f, 5f, 30f, inventory).orElseThrow();
        RanchUndoManager undo = new RanchUndoManager();
        undo.recordPlacement(placed);

        assertEquals(
            RanchUndoManager.UndoResult.PLACEMENT_REVERTED,
            undo.undo(state, inventory, (structure, x, z, heading) -> true)
        );
        assertTrue(state.structures().isEmpty());
        assertEquals(before, inventory.snapshot());
        assertFalse(undo.hasPending());
    }

    @Test
    void placementUndoRefusesChangedOrFilledStructure() {
        Inventory inventory = funded(HomesteadStructureType.GATE);
        HomesteadState state = new HomesteadState();
        PlacedStructure gate = state.place(HomesteadStructureType.GATE, 2f, 2f, 0f, inventory).orElseThrow();
        RanchUndoManager undo = new RanchUndoManager();
        undo.recordPlacement(gate);
        gate.toggleOpen();

        assertEquals(
            RanchUndoManager.UndoResult.STRUCTURE_CHANGED,
            undo.undo(state, inventory, (structure, x, z, heading) -> true)
        );
        assertEquals(1, state.structures().size());
        assertFalse(undo.hasPending());
    }

    @Test
    void relocationUndoRestoresOriginalTransformAfterValidation() {
        Inventory inventory = funded(HomesteadStructureType.CHEST);
        HomesteadState state = new HomesteadState();
        PlacedStructure chest = state.place(HomesteadStructureType.CHEST, 1f, 2f, 15f, inventory).orElseThrow();
        RanchUndoManager undo = new RanchUndoManager();

        assertTrue(state.relocate(chest.id(), 8f, 9f, 90f));
        undo.recordRelocation(chest, 1f, 2f, 15f, 8f, 9f, 90f);

        assertEquals(
            RanchUndoManager.UndoResult.RELOCATION_REVERTED,
            undo.undo(state, inventory, (structure, x, z, heading) -> x == 1f && z == 2f)
        );
        assertEquals(1f, chest.x());
        assertEquals(2f, chest.z());
        assertEquals(15f, chest.heading());
    }

    @Test
    void blockedRelocationUndoRemainsAvailable() {
        Inventory inventory = funded(HomesteadStructureType.FEEDER);
        HomesteadState state = new HomesteadState();
        PlacedStructure feeder = state.place(HomesteadStructureType.FEEDER, 1f, 1f, 0f, inventory).orElseThrow();
        state.relocate(feeder.id(), 7f, 7f, 45f);
        RanchUndoManager undo = new RanchUndoManager();
        undo.recordRelocation(feeder, 1f, 1f, 0f, 7f, 7f, 45f);

        assertEquals(
            RanchUndoManager.UndoResult.RESTORE_BLOCKED,
            undo.undo(state, inventory, (structure, x, z, heading) -> false)
        );
        assertTrue(undo.hasPending());
        assertEquals(7f, feeder.x());
    }

    private static Inventory funded(HomesteadStructureType type) {
        Inventory inventory = new Inventory();
        for (Map.Entry<ItemId, Integer> cost : type.buildCost().entrySet()) {
            inventory.add(cost.getKey(), cost.getValue());
        }
        return inventory;
    }
}
