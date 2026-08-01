// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Shared keyboard/controller navigation for Steam-friendly menus.
 */
final class MenuInputMapper {
    private static final float STICK_NAVIGATION_THRESHOLD = 0.62f;

    private final ControllerStateSource controllerSource;
    private final Supplier<MenuCommand> keyboardSource;
    private ControllerFrame previousController = ControllerFrame.disconnected();
    private InputDeviceType activeDevice = InputDeviceType.KEYBOARD_MOUSE;

    MenuInputMapper() {
        this(new GdxControllerStateSource(), MenuInputMapper::keyboardCommand);
    }

    MenuInputMapper(ControllerStateSource controllerSource, Supplier<MenuCommand> keyboardSource) {
        this.controllerSource = Objects.requireNonNull(controllerSource, "controllerSource");
        this.keyboardSource = Objects.requireNonNull(keyboardSource, "keyboardSource");
    }

    MenuInputSnapshot sample() {
        MenuCommand keyboard = Objects.requireNonNullElse(keyboardSource.get(), MenuCommand.idle());
        ControllerFrame current = Objects.requireNonNullElse(
            controllerSource.poll(),
            ControllerFrame.disconnected()
        );
        MenuCommand controller = controllerCommand(current, previousController);
        previousController = current;

        if (keyboard.hasActivity() && !controller.hasActivity()) {
            activeDevice = InputDeviceType.KEYBOARD_MOUSE;
        } else if (controller.hasActivity() && !keyboard.hasActivity()) {
            activeDevice = InputDeviceType.GAMEPAD;
        }

        if (keyboard.hasActivity() || controller.hasActivity()) {
            InputActivityTracker.record(activeDevice);
        }

        return new MenuInputSnapshot(keyboard.merge(controller), activeDevice);
    }

    void markPointerActive() {
        activeDevice = InputDeviceType.KEYBOARD_MOUSE;
        InputActivityTracker.record(activeDevice);
    }

    InputDeviceType activeDevice() {
        return activeDevice;
    }

    private static MenuCommand keyboardCommand() {
        return new MenuCommand(
            anyJustPressed(Input.Keys.UP, Input.Keys.W),
            anyJustPressed(Input.Keys.DOWN, Input.Keys.S),
            anyJustPressed(Input.Keys.LEFT, Input.Keys.A),
            anyJustPressed(Input.Keys.RIGHT, Input.Keys.D),
            anyJustPressed(Input.Keys.ENTER, Input.Keys.SPACE),
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
        );
    }

    private static MenuCommand controllerCommand(ControllerFrame current, ControllerFrame previous) {
        if (!current.connected()) return MenuCommand.idle();

        boolean currentUp = current.dpadUp() || current.leftStick().y() < -STICK_NAVIGATION_THRESHOLD;
        boolean previousUp = previous.dpadUp() || previous.leftStick().y() < -STICK_NAVIGATION_THRESHOLD;
        boolean currentDown = current.dpadDown() || current.leftStick().y() > STICK_NAVIGATION_THRESHOLD;
        boolean previousDown = previous.dpadDown() || previous.leftStick().y() > STICK_NAVIGATION_THRESHOLD;
        boolean currentLeft = current.dpadLeft() || current.leftStick().x() < -STICK_NAVIGATION_THRESHOLD;
        boolean previousLeft = previous.dpadLeft() || previous.leftStick().x() < -STICK_NAVIGATION_THRESHOLD;
        boolean currentRight = current.dpadRight() || current.leftStick().x() > STICK_NAVIGATION_THRESHOLD;
        boolean previousRight = previous.dpadRight() || previous.leftStick().x() > STICK_NAVIGATION_THRESHOLD;

        return new MenuCommand(
            justPressed(currentUp, previousUp),
            justPressed(currentDown, previousDown),
            justPressed(currentLeft, previousLeft),
            justPressed(currentRight, previousRight),
            justPressed(current.buttonA(), previous.buttonA())
                || justPressed(current.buttonStart(), previous.buttonStart()),
            justPressed(current.buttonB(), previous.buttonB())
                || justPressed(current.buttonBack(), previous.buttonBack())
        );
    }

    private static boolean anyJustPressed(int first, int second) {
        return Gdx.input.isKeyJustPressed(first) || Gdx.input.isKeyJustPressed(second);
    }

    private static boolean justPressed(boolean current, boolean previous) {
        return current && !previous;
    }
}
