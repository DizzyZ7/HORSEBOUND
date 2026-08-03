// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum InputDeviceType {
    KEYBOARD_MOUSE("input.device.keyboard_mouse"),
    GAMEPAD("input.device.gamepad"),
    STEAM_INPUT("input.device.steam_input");

    private final String displayKey;

    InputDeviceType(String displayKey) {
        this.displayKey = displayKey;
    }

    String displayName() {
        return I18n.text(displayKey);
    }
}
