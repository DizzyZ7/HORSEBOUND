// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

record InputSnapshot(PlayerCommand command, InputDeviceType activeDevice) {
    InputSnapshot {
        command = Objects.requireNonNullElse(command, PlayerCommand.idle());
        activeDevice = Objects.requireNonNullElse(activeDevice, InputDeviceType.KEYBOARD_MOUSE);
    }

    static InputSnapshot idle() {
        return new InputSnapshot(PlayerCommand.idle(), InputDeviceType.KEYBOARD_MOUSE);
    }
}
