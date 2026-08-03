// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class InputPromptCatalog {
    private InputPromptCatalog() {
    }

    static String gameplayHint(InputDeviceType device) {
        return I18n.text(device == InputDeviceType.KEYBOARD_MOUSE
            ? "prompt.game.keyboard"
            : "prompt.game.controller");
    }

    static String menuHint(InputDeviceType device) {
        return I18n.text(device == InputDeviceType.KEYBOARD_MOUSE
            ? "prompt.menu.keyboard"
            : "prompt.menu.controller");
    }

    static String savePrompt(InputDeviceType device) {
        return I18n.text(device == InputDeviceType.KEYBOARD_MOUSE ? "prompt.save.keyboard" : "prompt.save.controller");
    }
}
