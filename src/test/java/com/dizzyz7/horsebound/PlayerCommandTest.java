// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerCommandTest {
    @Test
    void clampsMovementAxesAndSanitizesInvalidLookInput() {
        PlayerCommand command = new PlayerCommand(
            2f,
            -4f,
            Float.NaN,
            Float.POSITIVE_INFINITY,
            false,
            false,
            false,
            false,
            false,
            false,
            false
        );

        assertEquals(1f, command.moveForward());
        assertEquals(-1f, command.moveRight());
        assertEquals(0f, command.lookYaw());
        assertEquals(0f, command.lookPitch());
    }

    @Test
    void mergesKeyboardMovementWithGamepadOrMouseLookAndButtonEdges() {
        PlayerCommand keyboard = new PlayerCommand(
            1f, 0f, 0f, 0f,
            true, false, true, false, false, false, false
        );
        PlayerCommand controller = new PlayerCommand(
            0f, 0.75f, 1.25f, -0.4f,
            false, true, false, true, false, false, false
        );

        PlayerCommand merged = keyboard.merge(controller);

        assertEquals(1f, merged.moveForward());
        assertEquals(0.75f, merged.moveRight());
        assertEquals(1.25f, merged.lookYaw());
        assertEquals(-0.4f, merged.lookPitch());
        assertTrue(merged.sprint());
        assertTrue(merged.jumpPressed());
        assertTrue(merged.interactPressed());
        assertTrue(merged.mountPressed());
        assertFalse(merged.buildPressed());
    }

    @Test
    void idleCommandContainsNoGameplayIntent() {
        PlayerCommand idle = PlayerCommand.idle();

        assertEquals(0f, idle.moveForward());
        assertEquals(0f, idle.moveRight());
        assertFalse(idle.sprint());
        assertFalse(idle.jumpPressed());
        assertFalse(idle.interactPressed());
        assertFalse(idle.pausePressed());
    }
}
