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
        assertEquals("F5", binding(PromptAction.SAVE, InputDeviceType.KEYBOARD_MOUSE, ControllerGlyphFamily.GENERIC));
        assertEquals("ESC", binding(PromptAction.PAUSE, InputDeviceType.KEYBOARD_MOUSE, ControllerGlyphFamily.GENERIC));
    }

    @Test
    void xboxAndDeckBindingsUseStandardFaceButtons() {
        assertEquals("A", binding(PromptAction.CONFIRM, InputDeviceType.GAMEPAD, ControllerGlyphFamily.XBOX));
        assertEquals("X", binding(PromptAction.INTERACT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.XBOX));
        assertEquals("Y", binding(PromptAction.MOUNT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.STEAM_DECK));
        assertEquals("LB", binding(PromptAction.BUILD, InputDeviceType.GAMEPAD, ControllerGlyphFamily.STEAM_DECK));
        assertEquals("RB", binding(PromptAction.SPRINT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.XBOX));
        assertEquals("VIEW", binding(PromptAction.SAVE, InputDeviceType.GAMEPAD, ControllerGlyphFamily.STEAM_DECK));
    }

    @Test
    void playStationBindingsUsePlayStationTerminology() {
        assertEquals("CROSS", binding(PromptAction.CONFIRM, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
        assertEquals("CIRCLE", binding(PromptAction.BACK, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
        assertEquals("SQUARE", binding(PromptAction.INTERACT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
        assertEquals("TRIANGLE", binding(PromptAction.MOUNT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
        assertEquals("SHARE", binding(PromptAction.SAVE, InputDeviceType.GAMEPAD, ControllerGlyphFamily.PLAYSTATION));
    }

    @Test
    void nintendoBindingsKeepSdlFaceLabelsAndPlatformShoulders() {
        assertEquals("A", binding(PromptAction.CONFIRM, InputDeviceType.GAMEPAD, ControllerGlyphFamily.NINTENDO));
        assertEquals("B", binding(PromptAction.BACK, InputDeviceType.GAMEPAD, ControllerGlyphFamily.NINTENDO));
        assertEquals("X", binding(PromptAction.INTERACT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.NINTENDO));
        assertEquals("Y", binding(PromptAction.MOUNT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.NINTENDO));
        assertEquals("L", binding(PromptAction.BUILD, InputDeviceType.GAMEPAD, ControllerGlyphFamily.NINTENDO));
        assertEquals("R", binding(PromptAction.SPRINT, InputDeviceType.GAMEPAD, ControllerGlyphFamily.NINTENDO));
        assertEquals("MINUS", binding(PromptAction.SAVE, InputDeviceType.GAMEPAD, ControllerGlyphFamily.NINTENDO));
    }

    private static String binding(
        PromptAction action,
        InputDeviceType device,
        ControllerGlyphFamily family
    ) {
        return InputGlyphCatalog.binding(action, device, family).glyph();
    }
}
