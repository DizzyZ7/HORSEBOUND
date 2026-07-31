// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Locale;

/** Controller glyph family inferred from the connected device name. */
enum ControllerGlyphFamily {
    XBOX,
    PLAYSTATION,
    STEAM_DECK,
    GENERIC;

    static ControllerGlyphFamily fromControllerName(String name) {
        if (name == null || name.isBlank()) return GENERIC;
        String normalized = name.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "steam deck", "neptune", "steam virtual gamepad")) {
            return STEAM_DECK;
        }
        if (containsAny(normalized, "dualsense", "dualshock", "playstation", "ps4", "ps5", "sony")) {
            return PLAYSTATION;
        }
        if (containsAny(normalized, "xbox", "xinput", "microsoft", "series x", "series s")) {
            return XBOX;
        }
        return GENERIC;
    }

    private static boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) return true;
        }
        return false;
    }
}
