// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamepadAccessibilityTest {
    @Test
    void invertCameraYChangesOnlyVerticalLookDirection() {
        ControllerFrame frame = frame(new AnalogStick(0.4f, 0.5f), false);
        InputProfile inverted = InputProfile.defaults().withInvertCameraY(true);
        GamepadInputMapper mapper = new GamepadInputMapper(() -> frame, () -> 1d / 60d, () -> inverted);

        PlayerCommand command = mapper.sample().command();

        assertTrue(command.lookYaw() > 0f);
        assertTrue(command.lookPitch() < 0f);
    }

    @Test
    void toggleSprintSurvivesReleaseAndStopsOnNextPressEdge() {
        List<ControllerFrame> frames = List.of(
            frame(AnalogStick.zero(), true),
            frame(AnalogStick.zero(), false),
            frame(AnalogStick.zero(), false),
            frame(AnalogStick.zero(), true)
        );
        AtomicInteger index = new AtomicInteger();
        InputProfile toggle = InputProfile.defaults().withSprintMode(SprintMode.TOGGLE);
        GamepadInputMapper mapper = new GamepadInputMapper(
            () -> frames.get(Math.min(index.getAndIncrement(), frames.size() - 1)),
            () -> 1d / 60d,
            () -> toggle
        );

        assertTrue(mapper.sample().command().sprint());
        assertTrue(mapper.sample().command().sprint());
        assertTrue(mapper.sample().command().sprint());
        assertFalse(mapper.sample().command().sprint());
    }

    private static ControllerFrame frame(AnalogStick right, boolean r1) {
        return new ControllerFrame(
            true,
            AnalogStick.zero(),
            right,
            false, false, false, false,
            false, false, false, r1,
            false, false, false, false
        );
    }
}
