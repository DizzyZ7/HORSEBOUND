// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerDeadZoneTest {
    @Test
    void removesSmallRadialStickDrift() {
        AnalogStick stick = ControllerDeadZone.radial(0.08f, -0.12f, 0.20f);

        assertEquals(AnalogStick.zero(), stick);
    }

    @Test
    void rescalesMovementAfterDeadZoneWithoutChangingDirection() {
        AnalogStick stick = ControllerDeadZone.radial(0.6f, 0.8f, 0.20f);

        assertEquals(0.6f, stick.x(), 0.0001f);
        assertEquals(0.8f, stick.y(), 0.0001f);
        assertEquals(1f, stick.magnitudeSquared(), 0.0001f);
    }

    @Test
    void normalizesSingleAxisAndInvalidValuesSafely() {
        assertEquals(0f, ControllerDeadZone.axis(0.10f, 0.20f));
        assertEquals(0.5f, ControllerDeadZone.axis(0.60f, 0.20f), 0.0001f);
        assertEquals(0f, ControllerDeadZone.axis(Float.NaN, 0.20f));
        assertTrue(ControllerDeadZone.axis(5f, 0.20f) <= 1f);
    }
}
