// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerCommandBufferTest {
    @Test
    void continuousMovementPersistsButEdgeActionsAreConsumedOnce() {
        PlayerCommandBuffer buffer = new PlayerCommandBuffer();
        buffer.submit(new PlayerCommand(
            1f, -0.25f, 2f, -1f,
            true, true, true, false, false, false, false
        ));

        PlayerCommand first = buffer.consumeForSimulationStep();
        PlayerCommand second = buffer.consumeForSimulationStep();

        assertEquals(1f, first.moveForward());
        assertEquals(-0.25f, first.moveRight());
        assertTrue(first.sprint());
        assertTrue(first.jumpPressed());
        assertTrue(first.interactPressed());
        assertEquals(2f, first.lookYaw());

        assertEquals(1f, second.moveForward());
        assertEquals(-0.25f, second.moveRight());
        assertTrue(second.sprint());
        assertFalse(second.jumpPressed());
        assertFalse(second.interactPressed());
        assertEquals(0f, second.lookYaw());
        assertEquals(0f, second.lookPitch());
    }

    @Test
    void accumulatesUnsampledLookAndButtonEdgesUntilNextSimulationTick() {
        PlayerCommandBuffer buffer = new PlayerCommandBuffer();
        buffer.submit(new PlayerCommand(0f, 0f, 0.5f, 0.25f, false, false, true, false, false, false, false));
        buffer.submit(new PlayerCommand(0f, 1f, 0.75f, -0.10f, false, true, false, true, false, false, false));

        PlayerCommand command = buffer.consumeForSimulationStep();

        assertEquals(1f, command.moveRight());
        assertEquals(1.25f, command.lookYaw());
        assertEquals(0.15f, command.lookPitch(), 0.0001f);
        assertTrue(command.jumpPressed());
        assertTrue(command.interactPressed());
        assertTrue(command.mountPressed());
    }

    @Test
    void resetClearsContinuousAndPendingIntent() {
        PlayerCommandBuffer buffer = new PlayerCommandBuffer();
        buffer.submit(new PlayerCommand(1f, 1f, 1f, 1f, true, true, true, true, true, true, true));
        buffer.reset();

        assertEquals(PlayerCommand.idle(), buffer.consumeForSimulationStep());
    }
}
