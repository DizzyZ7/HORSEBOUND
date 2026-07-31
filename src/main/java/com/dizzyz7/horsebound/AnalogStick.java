// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

record AnalogStick(float x, float y) {
    AnalogStick {
        x = finite(x);
        y = finite(y);
    }

    static AnalogStick zero() {
        return new AnalogStick(0f, 0f);
    }

    float magnitudeSquared() {
        return x * x + y * y;
    }

    private static float finite(float value) {
        return Float.isFinite(value) ? Math.max(-1f, Math.min(1f, value)) : 0f;
    }
}
