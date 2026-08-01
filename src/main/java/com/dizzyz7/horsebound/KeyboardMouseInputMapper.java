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
        PlayerCommand command = snapshot.command();

        if (command.buildPressed() && HomesteadInputContext.capturesBuild()) {
            HomesteadActionBus.requestBuild();
            command = command.withoutBuild();
        }
        if (command.interactPressed() && HomesteadInputContext.capturesInteract()) {
            HomesteadActionBus.requestInteract();
            command = command.withoutInteract();
        }
        if (command.mountPressed() && HomesteadInputContext.capturesMount()) {
            HomesteadActionBus.requestDismantle();
            command = command.withoutMount();
        }
        if (command.inventoryPressed() && HomesteadInputContext.capturesInventory()) {
            HomesteadActionBus.requestInventory();
            command = command.withoutInventory();
        }
        if (command.pausePressed()) {
            if (HomesteadInputContext.capturesPauseAsCancel()) {
                HomesteadActionBus.requestCancel();
            } else {
                PauseRequestBus.request();
            }
            command = command.withoutPause();
        }
        return command == snapshot.command()
            ? snapshot
            : new InputSnapshot(command, snapshot.activeDevice());
    }
}
