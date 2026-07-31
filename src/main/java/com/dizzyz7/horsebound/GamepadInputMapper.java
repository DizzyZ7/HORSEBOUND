// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;

import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Standard controller bindings:
 * A jump, X interact, Y mount, L1 build, R1 sprint/gallop,
 * Back manual save, Start or B pause/back.
 */
final class GamepadInputMapper implements InputMapper {
    private static final float LOOK_YAW_DEGREES_PER_SECOND = 150f;
    private static final float LOOK_PITCH_DEGREES_PER_SECOND = 110f;
    private static final float MAX_FRAME_DELTA = 0.05f;

    private final ControllerStateSource stateSource;
    private final DoubleSupplier frameDeltaSupplier;
    private ControllerFrame previous = ControllerFrame.disconnected();

    GamepadInputMapper() {
        this(new GdxControllerStateSource(), () -> Gdx.graphics.getDeltaTime());
    }

    GamepadInputMapper(ControllerStateSource stateSource, DoubleSupplier frameDeltaSupplier) {
        this.stateSource = Objects.requireNonNull(stateSource, "stateSource");
        this.frameDeltaSupplier = Objects.requireNonNull(frameDeltaSupplier, "frameDeltaSupplier");
    }

    @Override
    public InputSnapshot sample() {
        ControllerFrame current = Objects.requireNonNullElse(
            stateSource.poll(),
            ControllerFrame.disconnected()
        );
        if (!current.connected()) {
            previous = ControllerFrame.disconnected();
            return new InputSnapshot(PlayerCommand.idle(), InputDeviceType.GAMEPAD);
        }

        float frameDelta = safeFrameDelta(frameDeltaSupplier.getAsDouble());
        PlayerCommand command = new PlayerCommand(
            -current.leftStick().y(),
            current.leftStick().x(),
            current.rightStick().x() * LOOK_YAW_DEGREES_PER_SECOND * frameDelta,
            current.rightStick().y() * LOOK_PITCH_DEGREES_PER_SECOND * frameDelta,
            current.buttonR1(),
            justPressed(current.buttonA(), previous.buttonA()),
            justPressed(current.buttonX(), previous.buttonX()),
            justPressed(current.buttonY(), previous.buttonY()),
            justPressed(current.buttonL1(), previous.buttonL1()),
            justPressed(current.buttonBack(), previous.buttonBack()),
            justPressed(current.buttonStart(), previous.buttonStart())
                || justPressed(current.buttonB(), previous.buttonB())
        );

        previous = current;
        return new InputSnapshot(command, InputDeviceType.GAMEPAD);
    }

    private static boolean justPressed(boolean current, boolean previous) {
        return current && !previous;
    }

    private static float safeFrameDelta(double value) {
        if (!Double.isFinite(value) || value <= 0d) return 0f;
        return (float) Math.min(value, MAX_FRAME_DELTA);
    }
}
