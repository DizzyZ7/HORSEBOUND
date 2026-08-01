// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/** Standardized controller adapter with configurable dead zones, inversion, sprint mode and rumble. */
final class GamepadInputMapper implements InputMapper {
    private static final float LOOK_YAW_DEGREES_PER_SECOND = 150f;
    private static final float LOOK_PITCH_DEGREES_PER_SECOND = 110f;
    private static final float MAX_FRAME_DELTA = 0.05f;

    private final ControllerStateSource stateSource;
    private final DoubleSupplier frameDeltaSupplier;
    private final Supplier<InputProfile> profileSupplier;
    private final SprintLatch sprintLatch = new SprintLatch();
    private ControllerFrame previous = ControllerFrame.disconnected();

    GamepadInputMapper() {
        this(new GdxControllerStateSource(), () -> Gdx.graphics.getDeltaTime(), InputProfileContext::current);
    }

    GamepadInputMapper(ControllerStateSource stateSource, DoubleSupplier frameDeltaSupplier) {
        this(stateSource, frameDeltaSupplier, InputProfile::defaults);
    }

    GamepadInputMapper(
        ControllerStateSource stateSource,
        DoubleSupplier frameDeltaSupplier,
        Supplier<InputProfile> profileSupplier
    ) {
        this.stateSource = Objects.requireNonNull(stateSource, "stateSource");
        this.frameDeltaSupplier = Objects.requireNonNull(frameDeltaSupplier, "frameDeltaSupplier");
        this.profileSupplier = Objects.requireNonNull(profileSupplier, "profileSupplier");
    }

    @Override
    public InputSnapshot sample() {
        ControllerFrame current = Objects.requireNonNullElse(stateSource.poll(), ControllerFrame.disconnected());
        InputProfile profile = Objects.requireNonNullElse(profileSupplier.get(), InputProfile.defaults());
        if (!current.connected()) {
            previous = ControllerFrame.disconnected();
            sprintLatch.reset();
            return new InputSnapshot(PlayerCommand.idle(), InputDeviceType.GAMEPAD);
        }

        float frameDelta = safeFrameDelta(frameDeltaSupplier.getAsDouble());
        boolean jumpPressed = justPressed(current.buttonA(), previous.buttonA());
        boolean interactPressed = justPressed(current.buttonX(), previous.buttonX());
        boolean mountPressed = justPressed(current.buttonY(), previous.buttonY());
        boolean buildPressed = justPressed(current.buttonL1(), previous.buttonL1());
        boolean inventoryPressed = justPressed(current.dpadUp(), previous.dpadUp());
        boolean savePressed = justPressed(current.buttonBack(), previous.buttonBack());
        boolean pausePressed = justPressed(current.buttonStart(), previous.buttonStart())
            || justPressed(current.buttonB(), previous.buttonB());
        boolean sprint = sprintLatch.update(
            current.buttonR1(),
            justPressed(current.buttonR1(), previous.buttonR1()),
            profile.sprintMode()
        );
        float lookPitch = current.rightStick().y() * LOOK_PITCH_DEGREES_PER_SECOND * frameDelta;
        if (profile.invertCameraY()) lookPitch = -lookPitch;

        PlayerCommand command = new PlayerCommand(
            -current.leftStick().y(),
            current.leftStick().x(),
            current.rightStick().x() * LOOK_YAW_DEGREES_PER_SECOND * frameDelta,
            lookPitch,
            sprint,
            jumpPressed,
            interactPressed,
            mountPressed,
            buildPressed,
            inventoryPressed,
            savePressed,
            pausePressed
        );

        if (interactPressed || mountPressed || buildPressed || inventoryPressed) {
            ControllerRumble.pulse(profile, 65, 0.55f);
        } else if (jumpPressed) {
            ControllerRumble.pulse(profile, 35, 0.35f);
        } else if (savePressed) {
            ControllerRumble.pulse(profile, 45, 0.40f);
        }

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
