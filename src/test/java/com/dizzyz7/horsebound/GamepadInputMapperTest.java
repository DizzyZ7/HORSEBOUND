// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamepadInputMapperTest {
    @AfterEach
    void cleanupContext() {
        HomesteadInputContext.reset();
    }

    @Test
    void mapsAnalogMovementLookAndStandardGameplayButtons() {
        ControllerFrame held = frame(
            new AnalogStick(0.4f, -0.8f),
            new AnalogStick(0.5f, -0.25f),
            true, false, true, true,
            true, true, true, true
        );
        AtomicInteger frameIndex = new AtomicInteger();
        GamepadInputMapper mapper = new GamepadInputMapper(
            () -> frameIndex.getAndIncrement() == 0
                ? frame(AnalogStick.zero(), AnalogStick.zero(), false, false, false, false, false, false, false, false)
                : held,
            () -> 1d / 60d
        );

        assertEquals(PlayerCommand.idle(), mapper.sample().command());
        PlayerCommand first = mapper.sample().command();
        PlayerCommand second = mapper.sample().command();

        assertEquals(0.8f, first.moveForward());
        assertEquals(0.4f, first.moveRight());
        assertEquals(1.25f, first.lookYaw(), 0.0001f);
        assertEquals(-0.458333f, first.lookPitch(), 0.0001f);
        assertTrue(first.sprint());
        assertTrue(first.jumpPressed());
        assertTrue(first.interactPressed());
        assertTrue(first.mountPressed());
        assertTrue(first.buildPressed());
        assertTrue(first.savePressed());
        assertTrue(first.pausePressed());

        assertTrue(second.sprint());
        assertFalse(second.jumpPressed());
        assertFalse(second.interactPressed());
        assertFalse(second.mountPressed());
        assertFalse(second.buildPressed());
        assertFalse(second.savePressed());
        assertFalse(second.pausePressed());
    }

    @Test
    void dpadUpOpensInventoryOnlyOutsidePlacementContext() {
        ControllerFrame neutral = dpadFrame(false, false, false, false);
        ControllerFrame up = dpadFrame(true, false, false, false);
        AtomicInteger normalIndex = new AtomicInteger();
        GamepadInputMapper normal = new GamepadInputMapper(
            () -> normalIndex.getAndIncrement() == 0 ? neutral : up,
            () -> 1d / 60d
        );
        assertFalse(normal.sample().command().inventoryPressed());
        assertTrue(normal.sample().command().inventoryPressed());

        HomesteadInputContext.configure(true, true, true, true, true);
        AtomicInteger placementIndex = new AtomicInteger();
        GamepadInputMapper placement = new GamepadInputMapper(
            () -> placementIndex.getAndIncrement() == 0 ? neutral : up,
            () -> 1d / 60d
        );
        assertFalse(placement.sample().command().inventoryPressed());
        assertFalse(placement.sample().command().inventoryPressed());
    }

    @Test
    void suppressesCarriedButtonEdgesOnInitialConnectionAndReconnect() {
        ControllerFrame held = frame(
            AnalogStick.zero(), AnalogStick.zero(), true, false, false, false, false, false, false, false
        );
        ControllerFrame neutral = frame(
            AnalogStick.zero(), AnalogStick.zero(), false, false, false, false, false, false, false, false
        );
        List<ControllerFrame> sequence = List.of(
            held,
            neutral,
            held,
            ControllerFrame.disconnected(),
            held,
            neutral,
            held
        );
        AtomicInteger index = new AtomicInteger();
        GamepadInputMapper mapper = new GamepadInputMapper(
            () -> sequence.get(Math.min(index.getAndIncrement(), sequence.size() - 1)),
            () -> 1d / 60d
        );

        assertFalse(mapper.sample().command().jumpPressed());
        assertFalse(mapper.sample().command().jumpPressed());
        assertTrue(mapper.sample().command().jumpPressed());
        assertEquals(PlayerCommand.idle(), mapper.sample().command());
        assertFalse(mapper.sample().command().jumpPressed());
        assertFalse(mapper.sample().command().jumpPressed());
        assertTrue(mapper.sample().command().jumpPressed());
    }

    @Test
    void capsInvalidOrVeryLargeFrameDeltaForCameraLook() {
        ControllerFrame look = frame(
            AnalogStick.zero(),
            new AnalogStick(1f, 1f),
            false, false, false, false,
            false, false, false, false
        );
        GamepadInputMapper mapper = new GamepadInputMapper(() -> look, () -> 9d);

        PlayerCommand command = mapper.sample().command();

        assertEquals(7.5f, command.lookYaw(), 0.0001f);
        assertEquals(5.5f, command.lookPitch(), 0.0001f);
    }

    private static ControllerFrame frame(
        AnalogStick left,
        AnalogStick right,
        boolean a,
        boolean b,
        boolean x,
        boolean y,
        boolean back,
        boolean start,
        boolean l1,
        boolean r1
    ) {
        return new ControllerFrame(
            true,
            left,
            right,
            a, b, x, y,
            back, start, l1, r1,
            false, false, false, false
        );
    }

    private static ControllerFrame dpadFrame(boolean up, boolean down, boolean left, boolean right) {
        return new ControllerFrame(
            true,
            AnalogStick.zero(),
            AnalogStick.zero(),
            false, false, false, false,
            false, false, false, false,
            up, down, left, right
        );
    }
}
