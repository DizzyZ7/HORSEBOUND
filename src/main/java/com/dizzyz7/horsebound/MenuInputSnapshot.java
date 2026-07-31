// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

record MenuInputSnapshot(MenuCommand command, InputDeviceType activeDevice) {
    MenuInputSnapshot {
        command = Objects.requireNonNullElse(command, MenuCommand.idle());
        activeDevice = Objects.requireNonNullElse(activeDevice, InputDeviceType.KEYBOARD_MOUSE);
    }
}
