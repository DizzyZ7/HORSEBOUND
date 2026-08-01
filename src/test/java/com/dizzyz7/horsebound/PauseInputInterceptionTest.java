// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PauseInputInterceptionTest {
    @AfterEach
    void cleanUp() {
        PauseRequestBus.reset();
    }

    @Test
    void pauseBecomesApplicationRequestAndIsRemovedFromGameplayCommand() {
        PlayerCommand pause = new PlayerCommand(
            0f, 0f, 0f, 0f,
            false, false, false, false, false, false, true
        );
        KeyboardMouseInputMapper mapper = new KeyboardMouseInputMapper(
            () -> new InputSnapshot(pause, InputDeviceType.KEYBOARD_MOUSE),
            () -> new InputSnapshot(PlayerCommand.idle(), InputDeviceType.GAMEPAD)
        );

        InputSnapshot snapshot = mapper.sample();

        assertFalse(snapshot.command().pausePressed());
        assertTrue(PauseRequestBus.consume());
        assertFalse(PauseRequestBus.consume());
    }
}
