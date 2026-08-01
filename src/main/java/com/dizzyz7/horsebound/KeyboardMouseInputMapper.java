// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/**
 * Backward-compatible desktop input facade used by LivingRanchScreen.
 * It combines raw keyboard/mouse and standardized gamepad input into one command stream.
 */
final class KeyboardMouseInputMapper implements InputMapper {
    private final MixedInputMapper mixedInput;

    KeyboardMouseInputMapper(float mouseSensitivity) {
        mixedInput = new MixedInputMapper(
            new RawKeyboardMouseInputMapper(mouseSensitivity),
            new GamepadInputMapper()
        );
    }

    KeyboardMouseInputMapper(InputMapper keyboardMouse, InputMapper gamepad) {
        mixedInput = new MixedInputMapper(keyboardMouse, gamepad);
    }

    @Override
    public InputSnapshot sample() {
        InputSnapshot snapshot = mixedInput.sample();
        if (!snapshot.command().pausePressed()) return snapshot;
        PauseRequestBus.request();
        return new InputSnapshot(snapshot.command().withoutPause(), snapshot.activeDevice());
    }
}
