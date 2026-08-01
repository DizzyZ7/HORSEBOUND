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
        PlacedStructure gate = structure(HomesteadStructureType.GATE, 2f, 0f);

        HomesteadCollisionSystem.Position result = collisions.resolve(
            0f, 0f, 4f, 0f, 0.45f, List.of(gate)
        );

        assertTrue(result.blocked());
        assertTrue(result.x() < 1f);
    }

    @Test
    void openGateAllowsMovementThrough() {
        PlacedStructure gate = structure(HomesteadStructureType.GATE, 2f, 0f);
        gate.toggleOpen();

        HomesteadCollisionSystem.Position result = collisions.resolve(
            0f, 0f, 4f, 0f, 0.45f, List.of(gate)
        );

        assertFalse(result.blocked());
        assertEquals(4f, result.x());
    }

    @Test
    void largeFrameStepCannotTunnelThroughFence() {
        PlacedStructure fence = structure(HomesteadStructureType.FENCE, 5f, 0f);

        HomesteadCollisionSystem.Position result = collisions.resolve(
            -10f, 0f, 10f, 0f, 0.5f, List.of(fence)
        );

        assertTrue(result.blocked());
        assertTrue(result.x() < 4f);
    }

    @Test
    void actorInitiallyInsideClosedGateCanMoveOutButNotDeeper() {
        PlacedStructure gate = structure(HomesteadStructureType.GATE, 0f, 0f);

        HomesteadCollisionSystem.Position escape = collisions.resolve(
            0.2f, 0f, 3f, 0f, 0.45f, List.of(gate)
        );
        assertFalse(escape.blocked());
        assertEquals(3f, escape.x());

        HomesteadCollisionSystem.Position deeper = collisions.resolve(
            0.8f, 0f, 0f, 0f, 0.45f, List.of(gate)
        );
        assertTrue(deeper.blocked());
        assertTrue(deeper.x() > 0f);
    }

    @Test
    void stationaryActorInsideNewStructureIsPushedToSafety() {
        PlacedStructure chest = structure(HomesteadStructureType.CHEST, 0f, 0f);

        HomesteadCollisionSystem.Position result = collisions.resolve(
            0f, 0f, 0f, 0f, 0.45f, List.of(chest)
        );

        assertTrue(result.blocked());
        assertFalse(collisions.collides(result.x(), result.z(), 0.45f, List.of(chest)));
    }

    private static PlacedStructure structure(HomesteadStructureType type, float x, float z) {
        return new PlacedStructure(UUID.randomUUID(), type, x, z, 0f, 0);
    }
}
