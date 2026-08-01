// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

/** Current, profile-aware copy for the legacy world HUD. */
final class GameplayHudCopy {
    private GameplayHudCopy() {
    }

    static String buildLabel(BuildInfo buildInfo) {
        return Objects.requireNonNullElseGet(
            buildInfo,
            () -> new BuildInfo(null, null, null)
        ).displayLabel();
    }

    static String inputHint(InputDeviceType device, InputProfile profile) {
        InputDeviceType safeDevice = device == null ? InputDeviceType.KEYBOARD_MOUSE : device;
        InputProfile safeProfile = profile == null ? InputProfile.defaults() : profile;
        if (safeDevice == InputDeviceType.GAMEPAD || safeDevice == InputDeviceType.STEAM_INPUT) {
            return "Controller active | contextual glyphs below | Start pause";
        }
        return movementKeys(safeProfile) + " move | "
            + KeyLabel.of(safeProfile.interactKey()) + " use | "
            + KeyLabel.of(safeProfile.mountKey()) + " mount | "
            + KeyLabel.of(safeProfile.buildKey()) + " build | "
            + KeyLabel.of(safeProfile.inventoryKey()) + " inventory | "
            + KeyLabel.of(safeProfile.pauseKey()) + " pause";
    }

    private static String movementKeys(InputProfile profile) {
        return KeyLabel.of(profile.moveForwardKey()) + "/"
            + KeyLabel.of(profile.moveLeftKey()) + "/"
            + KeyLabel.of(profile.moveBackwardKey()) + "/"
            + KeyLabel.of(profile.moveRightKey());
    }
}
