// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputActivityTrackerTest {
    @AfterEach
    void resetTracker() {
        InputActivityTracker.reset();
    }

    @Test
    void defaultsToKeyboardAndGenericFamily() {
        InputActivityTracker.reset();
        assertEquals(InputDeviceType.KEYBOARD_MOUSE, InputActivityTracker.activeDevice());
        assertEquals(ControllerGlyphFamily.GENERIC, InputActivityTracker.controllerFamily());
    }

    @Test
    void remembersExplicitControllerFamily() {
        InputActivityTracker.record(InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION);
        assertEquals(InputDeviceType.GAMEPAD, InputActivityTracker.activeDevice());
        assertEquals(ControllerGlyphFamily.PLAYSTATION, InputActivityTracker.controllerFamily());
    }

    @Test
    void returningToKeyboardDoesNotEraseLastControllerFamily() {
        InputActivityTracker.record(InputDeviceType.GAMEPAD, ControllerGlyphFamily.STEAM_DECK);
        InputActivityTracker.record(InputDeviceType.KEYBOARD_MOUSE, ControllerGlyphFamily.GENERIC);
        assertEquals(InputDeviceType.KEYBOARD_MOUSE, InputActivityTracker.activeDevice());
        assertEquals(ControllerGlyphFamily.STEAM_DECK, InputActivityTracker.controllerFamily());
    }
}
