// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/**
 * Platform-independent snapshot produced from the standardized gdx-controllers mapping.
 */
record ControllerFrame(
    boolean connected,
    AnalogStick leftStick,
    AnalogStick rightStick,
    boolean buttonA,
    boolean buttonB,
    boolean buttonX,
    boolean buttonY,
    boolean buttonBack,
    boolean buttonStart,
    boolean buttonL1,
    boolean buttonR1,
    boolean dpadUp,
    boolean dpadDown,
    boolean dpadLeft,
    boolean dpadRight
) {
    ControllerFrame {
        leftStick = leftStick == null ? AnalogStick.zero() : leftStick;
        rightStick = rightStick == null ? AnalogStick.zero() : rightStick;
    }

    static ControllerFrame disconnected() {
        return new ControllerFrame(
            false,
            AnalogStick.zero(),
            AnalogStick.zero(),
            false, false, false, false,
            false, false, false, false,
            false, false, false, false
        );
    }

    boolean hasActivity() {
        return leftStick.magnitudeSquared() > 0.0004f
            || rightStick.magnitudeSquared() > 0.0004f
            || buttonA || buttonB || buttonX || buttonY
            || buttonBack || buttonStart || buttonL1 || buttonR1
            || dpadUp || dpadDown || dpadLeft || dpadRight;
    }
}
