// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Input;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameplayHudCopyTest {
    @Test
    void usesTheCurrentBuildMetadataInsteadOfALegacyLiteral() {
        assertEquals(
            "HORSEBOUND 0.5.6",
            GameplayHudCopy.buildLabel(new BuildInfo("0.5.6", "abcdef", AuthorInfo.CREATOR))
        );
    }

    @Test
    void keyboardHintReflectsRemappedBindings() {
        InputProfile profile = InputProfile.defaults()
            .withBinding(BindableAction.INTERACT, Input.Keys.T)
            .withBinding(BindableAction.INVENTORY, Input.Keys.O);

        String hint = GameplayHudCopy.inputHint(InputDeviceType.KEYBOARD_MOUSE, profile);

        assertTrue(hint.contains("T use"));
        assertTrue(hint.contains("O inventory"));
        assertFalse(hint.contains("glyph mapping in progress"));
    }

    @Test
    void controllerHintNoLongerClaimsGlyphsAreMissing() {
        String hint = GameplayHudCopy.inputHint(InputDeviceType.GAMEPAD, InputProfile.defaults());

        assertTrue(hint.contains("contextual glyphs below"));
        assertFalse(hint.contains("mapping in progress"));
    }
}
