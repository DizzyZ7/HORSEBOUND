// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControllerGlyphFamilyTest {
    @Test
    void detectsSteamDeckNamesBeforeGenericSteamNames() {
        assertEquals(ControllerGlyphFamily.STEAM_DECK,
            ControllerGlyphFamily.fromControllerName("Steam Deck Neptune Controller"));
        assertEquals(ControllerGlyphFamily.STEAM_DECK,
            ControllerGlyphFamily.fromControllerName("Steam Virtual Gamepad"));
    }

    @Test
    void detectsPlayStationAndXboxFamilies() {
        assertEquals(ControllerGlyphFamily.PLAYSTATION,
            ControllerGlyphFamily.fromControllerName("Sony DualSense Wireless Controller"));
        assertEquals(ControllerGlyphFamily.PLAYSTATION,
            ControllerGlyphFamily.fromControllerName("DUALSHOCK 4"));
        assertEquals(ControllerGlyphFamily.XBOX,
            ControllerGlyphFamily.fromControllerName("Xbox Series X Controller"));
        assertEquals(ControllerGlyphFamily.XBOX,
            ControllerGlyphFamily.fromControllerName("XInput Controller #1"));
    }

    @Test
    void unknownOrMissingNamesRemainGeneric() {
        assertEquals(ControllerGlyphFamily.GENERIC,
            ControllerGlyphFamily.fromControllerName("8BitDo Wireless Controller"));
        assertEquals(ControllerGlyphFamily.GENERIC,
            ControllerGlyphFamily.fromControllerName(null));
        assertEquals(ControllerGlyphFamily.GENERIC,
            ControllerGlyphFamily.fromControllerName(" "));
    }
}
