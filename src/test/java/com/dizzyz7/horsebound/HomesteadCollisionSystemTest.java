// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomesteadCollisionSystemTest {
    private final HomesteadCollisionSystem collisions = new HomesteadCollisionSystem();

    @Test
    void sweptMovementStopsBeforeClosedGate() {
        PlacedStructure gate = new PlacedStructure(
            UUID.randomUUID(),
            HomesteadStructureType.GATE,
            2f,
            0f,
            0f,
            0
        );

        HomesteadCollisionSystem.Position result = collisions.resolve(
            0f,
            0f,
            4f,
            0f,
            0.45f,
            List.of(gate)
        );

        assertTrue(result.blocked());
        assertTrue(result.x() < 1f);
    }

    @Test
    void openGateAllowsMovementThrough() {
        PlacedStructure gate = new PlacedStructure(
            UUID.randomUUID(),
            HomesteadStructureType.GATE,
            2f,
            0f,
            0f,
            0
        );
        gate.toggleOpen();

        HomesteadCollisionSystem.Position result = collisions.resolve(
            0f,
            0f,
            4f,
            0f,
            0.45f,
            List.of(gate)
        );

        assertFalse(result.blocked());
        assertEquals(4f, result.x());
    }

    @Test
    void largeFrameStepCannotTunnelThroughFence() {
        PlacedStructure fence = new PlacedStructure(
            UUID.randomUUID(),
            HomesteadStructureType.FENCE,
            5f,
            0f,
            0f,
            0
        );

        HomesteadCollisionSystem.Position result = collisions.resolve(
            -10f,
            0f,
            10f,
            0f,
            0.5f,
            List.of(fence)
        );

        assertTrue(result.blocked());
        assertTrue(result.x() < 4f);
    }
}
