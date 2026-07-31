// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiScaleTest {
    @Test
    void deckNativeResolutionUsesOneToOneScale() {
        assertEquals(1f, UiScale.effective(1280, 800, 1f), 0.0001f);
    }

    @Test
    void smallerWindowsKeepAReadableMinimum() {
        assertTrue(UiScale.effective(960, 600, 1f) >= UiScale.MIN_EFFECTIVE_SCALE);
    }

    @Test
    void userScaleCanEnlargeDeckText() {
        assertEquals(1.4f, UiScale.effective(1280, 800, 1.4f), 0.0001f);
    }

    @Test
    void invalidScaleFallsBackSafely() {
        assertEquals(1f, UiScale.effective(1280, 800, Float.NaN), 0.0001f);
    }
}
