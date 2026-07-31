// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputPromptCatalogTest {
    @Test
    void keyboardPromptsDoNotShowControllerButtons() {
        String hint = InputPromptCatalog.gameplayHint(InputDeviceType.KEYBOARD_MOUSE);
        assertTrue(hint.contains("[WASD]"));
        assertTrue(hint.contains("[E]"));
        assertFalse(hint.contains("[LS]"));
    }

    @Test
    void controllerPromptsDoNotShowKeyboardKeys() {
        String hint = InputPromptCatalog.gameplayHint(InputDeviceType.GAMEPAD);
        assertTrue(hint.contains("[LS]"));
        assertTrue(hint.contains("[X]"));
        assertFalse(hint.contains("[WASD]"));
    }

    @Test
    void menuPromptsFollowActiveDevice() {
        assertTrue(InputPromptCatalog.menuHint(InputDeviceType.KEYBOARD_MOUSE).contains("[Enter]"));
        assertTrue(InputPromptCatalog.menuHint(InputDeviceType.STEAM_INPUT).contains("[A]"));
    }
}
