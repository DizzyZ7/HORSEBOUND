// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputGlyphCatalogTest {
    @Test
    void keyboardBindingsUseActualGameplayKeys() {
        assertEquals("E", binding(PromptAction.INTERACT, InputDeviceType.KEYBOARD_MOUSE, ControllerGlyphFamily.GENERIC));
        assertEquals("F", binding(PromptAction.MOUNT, InputDeviceType.KEYBOARD_MOUSE, ControllerGlyphFamily.GENERIC));
        assertEquals("SPACE", binding(PromptAction.JUMP, InputDeviceType.KEYBOARD_MOUSE, ControllerGlyphFamily.GENERIC));
        assertEquals("SHIFT", binding(PromptAction.SPRINT, InputDeviceType.KEYBOARD_MOUSE, ControllerGlyphFamily.GENERIC));
        assertEquals("ESC", binding(PromptAction.PAUSE, InputDeviceType.KEYBOARD_MOUSE, ControllerGlyphFamily.GENERIC));
    }

    @Test
    void xboxAndDeckBindingsUseStandardFaceButtons() {
        assertEquals("A", binding(PromptAction.CONFIRM, InputDeviceType.GAMEPAD, ControllerGlyphFamily.XBOX));
        assertEquals("X", binding(PromptAction.INTERACT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.XBOX));
        assertEquals("Y", binding(PromptAction.MOUNT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.STEAM_DECK));
        assertEquals("LB", binding(PromptAction.BUILD, InputDeviceType.GAMEPAD, ControllerGlyphFamily.STEAM_DECK));
        assertEquals("RB", binding(PromptAction.SPRINT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.XBOX));
    }

    @Test
    void playStationBindingsUsePlayStationTerminology() {
        assertEquals("CROSS", binding(PromptAction.CONFIRM, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
        assertEquals("CIRCLE", binding(PromptAction.BACK, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
        assertEquals("SQUARE", binding(PromptAction.INTERACT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
        assertEquals("TRIANGLE", binding(PromptAction.MOUNT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
        assertEquals("SHARE", binding(PromptAction.SAVE, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
    }

    private static String binding(
        PromptAction action,
        InputDeviceType device,
        ControllerGlyphFamily family
    ) {
        return InputGlyphCatalog.binding(action, device, family).glyph();
    }
}
