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
    void emitsControllerNavigationAndConfirmOnlyOnEdges() {
        ControllerFrame held = frame(new AnalogStick(0f, 0.8f), true, false, false);
        List<ControllerFrame> frames = List.of(held, held, frame(AnalogStick.zero(), false, false, false));
        AtomicInteger index = new AtomicInteger();
        ControllerStateSource source = () -> frames.get(Math.min(index.getAndIncrement(), frames.size() - 1));
        MenuInputMapper mapper = new MenuInputMapper(source, MenuCommand::idle);

        MenuInputSnapshot first = mapper.sample();
        MenuInputSnapshot second = mapper.sample();

        assertTrue(first.command().downPressed());
        assertTrue(first.command().confirmPressed());
        assertEquals(InputDeviceType.GAMEPAD, first.activeDevice());
        assertFalse(second.command().downPressed());
        assertFalse(second.command().confirmPressed());
    }

    @Test
    void heldDirectionRepeatsAfterDelayButConfirmNeverRepeats() {
        ControllerFrame held = frame(new AnalogStick(0f, 0.8f), true, false, false);
        MenuInputMapper mapper = new MenuInputMapper(() -> held, MenuCommand::idle, () -> 0.10d);

        assertTrue(mapper.sample().command().downPressed());
        assertTrue(mapper.sample().command().confirmPressed() == false);
        assertFalse(mapper.sample().command().downPressed());
        assertFalse(mapper.sample().command().downPressed());
        assertTrue(mapper.sample().command().downPressed());
        assertFalse(mapper.sample().command().confirmPressed());
    }

    @Test
    void supportsDpadAndBackAndReturnsPromptsToKeyboardWhenUsed() {
        List<ControllerFrame> frames = List.of(
            frame(AnalogStick.zero(), false, false, true),
            frame(AnalogStick.zero(), false, false, false)
        );
        AtomicInteger controllerIndex = new AtomicInteger();
        ControllerStateSource source = () -> frames.get(Math.min(controllerIndex.getAndIncrement(), frames.size() - 1));
        AtomicInteger keyboardIndex = new AtomicInteger();
        Supplier<MenuCommand> keyboard = () -> keyboardIndex.getAndIncrement() == 0
            ? MenuCommand.idle()
            : new MenuCommand(true, false, false, false, false, false);
        MenuInputMapper mapper = new MenuInputMapper(source, keyboard);

        MenuInputSnapshot controller = mapper.sample();
        MenuInputSnapshot keyboardReturn = mapper.sample();

        assertTrue(controller.command().backPressed());
        assertEquals(InputDeviceType.GAMEPAD, controller.activeDevice());
        assertTrue(keyboardReturn.command().upPressed());
        assertEquals(InputDeviceType.KEYBOARD_MOUSE, keyboardReturn.activeDevice());
    }

    @Test
    void pointerActivityForcesKeyboardMousePrompts() {
        MenuInputMapper mapper = new MenuInputMapper(
            () -> frame(AnalogStick.zero(), true, false, false),
            MenuCommand::idle
        );

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
