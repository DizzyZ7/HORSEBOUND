// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

final class RawKeyboardMouseInputMapper implements InputMapper {
    private final float mouseSensitivity;

    RawKeyboardMouseInputMapper(float mouseSensitivity) {
        this.mouseSensitivity = Float.isFinite(mouseSensitivity)
            ? Math.max(0.01f, Math.min(2f, mouseSensitivity))
            : 0.20f;
    }

    @Override
    public InputSnapshot sample() {
        PlayerCommand command = new PlayerCommand(
            axis(Input.Keys.W, Input.Keys.S),
            axis(Input.Keys.D, Input.Keys.A),
            -Gdx.input.getDeltaX() * mouseSensitivity,
            Gdx.input.getDeltaY() * mouseSensitivity * 0.75f,
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT),
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE),
            Gdx.input.isKeyJustPressed(Input.Keys.E),
            Gdx.input.isKeyJustPressed(Input.Keys.F),
            Gdx.input.isKeyJustPressed(Input.Keys.B),
            Gdx.input.isKeyJustPressed(Input.Keys.F5),
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
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
