// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;

import java.util.Objects;
import java.util.function.Supplier;

final class RawKeyboardMouseInputMapper implements InputMapper {
    private final float mouseSensitivity;
    private final Supplier<InputProfile> profileSupplier;
    private final SprintLatch sprintLatch = new SprintLatch();

    RawKeyboardMouseInputMapper(float mouseSensitivity) {
        this(mouseSensitivity, InputProfileContext::current);
    }

    RawKeyboardMouseInputMapper(float mouseSensitivity, Supplier<InputProfile> profileSupplier) {
        this.mouseSensitivity = Float.isFinite(mouseSensitivity)
            ? Math.max(0.01f, Math.min(2f, mouseSensitivity))
            : 0.20f;
        this.profileSupplier = Objects.requireNonNull(profileSupplier, "profileSupplier");
    }

    @Override
    public InputSnapshot sample() {
        InputProfile profile = Objects.requireNonNullElse(profileSupplier.get(), InputProfile.defaults());
        boolean sprintPressed = Gdx.input.isKeyPressed(profile.sprintKey());
        boolean sprint = sprintLatch.update(
            sprintPressed,
            Gdx.input.isKeyJustPressed(profile.sprintKey()),
            profile.sprintMode()
        );
        float pitch = Gdx.input.getDeltaY() * mouseSensitivity * 0.75f;
        if (profile.invertCameraY()) pitch = -pitch;

        PlayerCommand command = new PlayerCommand(
            axis(profile.moveForwardKey(), profile.moveBackwardKey()),
            axis(profile.moveRightKey(), profile.moveLeftKey()),
            -Gdx.input.getDeltaX() * mouseSensitivity,
            pitch,
            sprint,
            Gdx.input.isKeyJustPressed(profile.jumpKey()),
            Gdx.input.isKeyJustPressed(profile.interactKey()),
            Gdx.input.isKeyJustPressed(profile.mountKey()),
            Gdx.input.isKeyJustPressed(profile.buildKey()),
            Gdx.input.isKeyJustPressed(profile.saveKey()),
            Gdx.input.isKeyJustPressed(profile.pauseKey())
        );
        return new InputSnapshot(command, InputDeviceType.KEYBOARD_MOUSE);
    }

    private static float axis(int positiveKey, int negativeKey) {
        float value = 0f;
        if (Gdx.input.isKeyPressed(positiveKey)) value += 1f;
        if (Gdx.input.isKeyPressed(negativeKey)) value -= 1f;
        return value;
    }
}
