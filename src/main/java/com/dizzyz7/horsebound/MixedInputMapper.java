// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

/**
 * Supports simultaneous keyboard/mouse and controller input while keeping prompts on the last meaningfully used device.
 */
final class MixedInputMapper implements InputMapper {
    private final InputMapper keyboardMouse;
    private final InputMapper gamepad;
    private InputDeviceType activeDevice = InputDeviceType.KEYBOARD_MOUSE;

    MixedInputMapper(InputMapper keyboardMouse, InputMapper gamepad) {
        this.keyboardMouse = Objects.requireNonNull(keyboardMouse, "keyboardMouse");
        this.gamepad = Objects.requireNonNull(gamepad, "gamepad");
    }

    @Override
    public InputSnapshot sample() {
        InputSnapshot keyboard = Objects.requireNonNullElse(keyboardMouse.sample(), InputSnapshot.idle());
        InputSnapshot controller = Objects.requireNonNullElse(gamepad.sample(), InputSnapshot.idle());

        boolean keyboardActive = keyboard.command().hasActivity();
        boolean controllerActive = controller.command().hasActivity();

        if (keyboardActive && !controllerActive) {
            activeDevice = InputDeviceType.KEYBOARD_MOUSE;
        } else if (controllerActive && !keyboardActive) {
            activeDevice = controller.activeDevice();
        } else if (keyboardActive && controllerActive) {
            if (activeDevice != InputDeviceType.KEYBOARD_MOUSE
                && activeDevice != controller.activeDevice()) {
                activeDevice = controller.activeDevice();
            }
        }

        if (keyboardActive || controllerActive) {
            InputActivityTracker.record(activeDevice);
        }

        return new InputSnapshot(
            keyboard.command().merge(controller.command()),
            activeDevice
        );
    }
}
