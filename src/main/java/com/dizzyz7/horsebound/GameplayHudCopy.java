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
            return I18n.text("hud.controller_active");
        }
        return I18n.text(
            "hud.keyboard_controls",
            movementKeys(safeProfile),
            KeyLabel.of(safeProfile.interactKey()),
            KeyLabel.of(safeProfile.mountKey()),
            KeyLabel.of(safeProfile.buildKey()),
            KeyLabel.of(safeProfile.inventoryKey()),
            KeyLabel.of(safeProfile.pauseKey())
        );
    }

    private static String movementKeys(InputProfile profile) {
        return KeyLabel.of(profile.moveForwardKey()) + "/"
            + KeyLabel.of(profile.moveLeftKey()) + "/"
            + KeyLabel.of(profile.moveBackwardKey()) + "/"
            + KeyLabel.of(profile.moveRightKey());
    }
}
