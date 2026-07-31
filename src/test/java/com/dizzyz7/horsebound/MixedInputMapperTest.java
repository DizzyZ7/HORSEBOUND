// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MixedInputMapperTest {
    @Test
    void keepsKeyboardMovementWhileUsingControllerCameraAndAction() {
        InputMapper keyboard = () -> new InputSnapshot(
            new PlayerCommand(1f, 0f, 0f, 0f, false, false, false, false, false, false, false),
            InputDeviceType.KEYBOARD_MOUSE
        );
        InputMapper controller = () -> new InputSnapshot(
            new PlayerCommand(0f, 0f, 2f, -1f, false, true, false, false, false, false, false),
            InputDeviceType.GAMEPAD
        );
        MixedInputMapper mapper = new MixedInputMapper(keyboard, controller);

        InputSnapshot mixed = mapper.sample();

        assertEquals(1f, mixed.command().moveForward());
        assertEquals(2f, mixed.command().lookYaw());
        assertEquals(-1f, mixed.command().lookPitch());
        assertTrue(mixed.command().jumpPressed());
    }

    @Test
    void changesPromptsOnlyWhenADeviceHasMeaningfulActivity() {
        List<InputSnapshot> keyboardFrames = List.of(
            InputSnapshot.idle(),
            new InputSnapshot(
                new PlayerCommand(1f, 0f, 0f, 0f, false, false, false, false, false, false, false),
                InputDeviceType.KEYBOARD_MOUSE
            )
        );
        List<InputSnapshot> controllerFrames = List.of(
            new InputSnapshot(
                new PlayerCommand(0f, 0f, 1f, 0f, false, false, false, false, false, false, false),
                InputDeviceType.GAMEPAD
            ),
            InputSnapshot.idle()
        );
        AtomicInteger index = new AtomicInteger();
        InputMapper keyboard = () -> keyboardFrames.get(Math.min(index.get(), keyboardFrames.size() - 1));
        InputMapper controller = () -> controllerFrames.get(Math.min(index.getAndIncrement(), controllerFrames.size() - 1));
        MixedInputMapper mapper = new MixedInputMapper(keyboard, controller);

        assertEquals(InputDeviceType.GAMEPAD, mapper.sample().activeDevice());
        assertEquals(InputDeviceType.KEYBOARD_MOUSE, mapper.sample().activeDevice());
    }
}
