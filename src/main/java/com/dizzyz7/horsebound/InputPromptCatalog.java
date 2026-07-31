// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class InputPromptCatalog {
    private InputPromptCatalog() {
    }

    static String gameplayHint(InputDeviceType device) {
        return switch (device) {
            case KEYBOARD_MOUSE ->
                "[WASD] Move  [Mouse] Camera  [E] Interact  [F] Mount  [B] Build  [Shift] Gallop  [Space] Jump";
            case GAMEPAD, STEAM_INPUT ->
                "[LS] Move  [RS] Camera  [X] Interact  [Y] Mount  [LB] Build  [RB] Gallop  [A] Jump";
        };
    }

    static String menuHint(InputDeviceType device) {
        return device == InputDeviceType.KEYBOARD_MOUSE
            ? "[Arrows/WASD] Select  [Enter] Confirm  [Esc] Back"
            : "[D-pad/LS] Select  [A] Confirm  [B] Back";
    }

    static String savePrompt(InputDeviceType device) {
        return device == InputDeviceType.KEYBOARD_MOUSE ? "[F5] Save" : "[View] Save";
    }
}
