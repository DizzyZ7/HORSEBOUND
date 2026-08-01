// HORSEBOUND — Created by Dimash Janibekov (DizZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Input;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyLabelTest {
    @Test
    void modifiersAndSystemKeysUseStableLabels() {
        assertEquals("SHIFT", KeyLabel.of(Input.Keys.SHIFT_LEFT));
        assertEquals("SHIFT", KeyLabel.of(Input.Keys.SHIFT_RIGHT));
        assertEquals("CTRL", KeyLabel.of(Input.Keys.CONTROL_LEFT));
        assertEquals("ESC", KeyLabel.of(Input.Keys.ESCAPE));
        assertEquals("SPACE", KeyLabel.of(Input.Keys.SPACE));
    }
}
