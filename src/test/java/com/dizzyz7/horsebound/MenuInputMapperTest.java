// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuInputMapperTest {
    @Test
    void suppressesCarriedControllerInputUntilTheControllerReturnsToNeutral() {
        ControllerFrame held = frame(new AnalogStick(0f, 0.8f), true, false, false);
        ControllerFrame neutral = frame(AnalogStick.zero(), false, false, false);
        List<ControllerFrame> frames = List.of(held, held, neutral, held, held);
        AtomicInteger index = new AtomicInteger();
        MenuInputMapper mapper = new MenuInputMapper(
            () -> frames.get(Math.min(index.getAndIncrement(), frames.size() - 1)),
            MenuCommand::idle,
            () -> 1d / 60d
        );

        MenuInputSnapshot first = mapper.sample();
        MenuInputSnapshot carried = mapper.sample();
        MenuInputSnapshot released = mapper.sample();
        MenuInputSnapshot freshPress = mapper.sample();
        MenuInputSnapshot stillHeld = mapper.sample();

        assertFalse(first.command().hasActivity());
        assertEquals(InputDeviceType.GAMEPAD, first.activeDevice());
        assertFalse(carried.command().hasActivity());
        assertFalse(released.command().hasActivity());
        assertTrue(freshPress.command().downPressed());
        assertTrue(freshPress.command().confirmPressed());
        assertFalse(stillHeld.command().downPressed());
        assertFalse(stillHeld.command().confirmPressed());
    }

    @Test
    void heldDirectionRepeatsAfterDelayButConfirmNeverRepeats() {
        ControllerFrame neutral = frame(AnalogStick.zero(), false, false, false);
        ControllerFrame held = frame(new AnalogStick(0f, 0.8f), true, false, false);
        AtomicInteger index = new AtomicInteger();
        MenuInputMapper mapper = new MenuInputMapper(
            () -> index.getAndIncrement() == 0 ? neutral : held,
            MenuCommand::idle,
            () -> 0.10d
        );

        assertFalse(mapper.sample().command().hasActivity());
        MenuCommand firstPress = mapper.sample().command();
        assertTrue(firstPress.downPressed());
        assertTrue(firstPress.confirmPressed());
        assertFalse(mapper.sample().command().downPressed());
        assertFalse(mapper.sample().command().downPressed());
        assertFalse(mapper.sample().command().downPressed());
        assertTrue(mapper.sample().command().downPressed());
        assertFalse(mapper.sample().command().confirmPressed());
    }

    @Test
    void supportsBackAndReturnsPromptsToKeyboardWhenUsed() {
        List<ControllerFrame> frames = List.of(
            frame(AnalogStick.zero(), false, false, false),
            frame(AnalogStick.zero(), false, false, true),
            frame(AnalogStick.zero(), false, false, false)
        );
        AtomicInteger controllerIndex = new AtomicInteger();
        ControllerStateSource source = () -> frames.get(Math.min(controllerIndex.getAndIncrement(), frames.size() - 1));
        AtomicInteger keyboardIndex = new AtomicInteger();
        Supplier<MenuCommand> keyboard = () -> keyboardIndex.getAndIncrement() < 2
            ? MenuCommand.idle()
            : new MenuCommand(true, false, false, false, false, false);
        MenuInputMapper mapper = new MenuInputMapper(source, keyboard);

        assertFalse(mapper.sample().command().hasActivity());
        MenuInputSnapshot controller = mapper.sample();
        MenuInputSnapshot keyboardReturn = mapper.sample();

        assertTrue(controller.command().backPressed());
        assertEquals(InputDeviceType.GAMEPAD, controller.activeDevice());
        assertTrue(keyboardReturn.command().upPressed());
        assertEquals(InputDeviceType.KEYBOARD_MOUSE, keyboardReturn.activeDevice());
    }

    @Test
    void keyboardRemainsResponsiveDuringControllerPriming() {
        ControllerFrame held = frame(AnalogStick.zero(), true, false, false);
        MenuInputMapper mapper = new MenuInputMapper(
            () -> held,
            () -> new MenuCommand(false, true, false, false, false, false)
        );

        MenuInputSnapshot snapshot = mapper.sample();

        assertTrue(snapshot.command().downPressed());
        assertFalse(snapshot.command().confirmPressed());
        assertEquals(InputDeviceType.KEYBOARD_MOUSE, snapshot.activeDevice());
    }

    @Test
    void pointerActivityForcesKeyboardMousePrompts() {
        AtomicInteger index = new AtomicInteger();
        MenuInputMapper mapper = new MenuInputMapper(
            () -> index.getAndIncrement() == 0
                ? frame(AnalogStick.zero(), false, false, false)
                : frame(AnalogStick.zero(), true, false, false),
            MenuCommand::idle
        );

        mapper.sample();
        assertEquals(InputDeviceType.GAMEPAD, mapper.sample().activeDevice());
        mapper.markPointerActive();
        assertEquals(InputDeviceType.KEYBOARD_MOUSE, mapper.activeDevice());
    }

    private static ControllerFrame frame(
        AnalogStick left,
        boolean a,
        boolean dpadRight,
        boolean b
    ) {
        return new ControllerFrame(
            true,
            left,
            AnalogStick.zero(),
            a, b, false, false,
            false, false, false, false,
            false, false, false, dpadRight
        );
    }
}
