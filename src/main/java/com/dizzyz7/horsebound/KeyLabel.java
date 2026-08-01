// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Input;

import java.util.Locale;

/** Stable player-facing keyboard labels independent of backend-specific key names. */
final class KeyLabel {
    private KeyLabel() {
    }

    static String of(int keyCode) {
        return switch (keyCode) {
            case Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT -> "SHIFT";
            case Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT -> "CTRL";
            case Input.Keys.ALT_LEFT, Input.Keys.ALT_RIGHT -> "ALT";
            case Input.Keys.ESCAPE -> "ESC";
            case Input.Keys.SPACE -> "SPACE";
            case Input.Keys.ENTER -> "ENTER";
            case Input.Keys.BACKSPACE -> "BACKSPACE";
            case Input.Keys.TAB -> "TAB";
            default -> normalizedBackendName(keyCode);
        };
    }

    private static String normalizedBackendName(int keyCode) {
        String value = Input.Keys.toString(keyCode);
        if (value == null || value.isBlank()) return "KEY " + keyCode;
        return value.trim().replace('_', ' ').toUpperCase(Locale.ROOT);
    }
}
