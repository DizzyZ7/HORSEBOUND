// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class ControllerDeadZone {
    private ControllerDeadZone() {
    }

    static AnalogStick radial(float rawX, float rawY, float deadZone) {
        float x = finiteAxis(rawX);
        float y = finiteAxis(rawY);
        float safeDeadZone = Float.isFinite(deadZone)
            ? Math.max(0f, Math.min(0.95f, deadZone))
            : 0.20f;

        float magnitude = (float) Math.sqrt(x * x + y * y);
        if (magnitude <= safeDeadZone || magnitude <= 0.00001f) {
            return AnalogStick.zero();
        }

        float normalizedMagnitude = Math.min(1f, (magnitude - safeDeadZone) / (1f - safeDeadZone));
        float scale = normalizedMagnitude / magnitude;
        return new AnalogStick(x * scale, y * scale);
    }

    static float axis(float rawValue, float deadZone) {
        float value = finiteAxis(rawValue);
        float safeDeadZone = Float.isFinite(deadZone)
            ? Math.max(0f, Math.min(0.95f, deadZone))
            : 0.20f;
        float magnitude = Math.abs(value);
        if (magnitude <= safeDeadZone) return 0f;
        float normalized = Math.min(1f, (magnitude - safeDeadZone) / (1f - safeDeadZone));
        return Math.copySign(normalized, value);
    }

    private static float finiteAxis(float value) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(-1f, Math.min(1f, value));
    }
}
