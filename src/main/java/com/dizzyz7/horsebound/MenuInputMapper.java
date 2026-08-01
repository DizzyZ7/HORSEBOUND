// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * Shared keyboard/controller navigation for Steam-friendly menus.
 */
final class MenuInputMapper {
    private static final float STICK_NAVIGATION_THRESHOLD = 0.62f;

    private final ControllerStateSource controllerSource;
    private final Supplier<MenuCommand> keyboardSource;
    private final DoubleSupplier frameDeltaSupplier;
    private final NavigationRepeater navigationRepeater = new NavigationRepeater();
    private ControllerFrame previousController = ControllerFrame.disconnected();
    private InputDeviceType activeDevice = InputDeviceType.KEYBOARD_MOUSE;

    MenuInputMapper() {
        this(
            new GdxControllerStateSource(),
            MenuInputMapper::keyboardCommand,
            MenuInputMapper::currentFrameDelta
        );
    }

    MenuInputMapper(ControllerStateSource controllerSource, Supplier<MenuCommand> keyboardSource) {
        this(controllerSource, keyboardSource, MenuInputMapper::currentFrameDelta);
    }

    MenuInputMapper(
        ControllerStateSource controllerSource,
        Supplier<MenuCommand> keyboardSource,
        DoubleSupplier frameDeltaSupplier
    ) {
        this.controllerSource = Objects.requireNonNull(controllerSource, "controllerSource");
        this.keyboardSource = Objects.requireNonNull(keyboardSource, "keyboardSource");
        this.frameDeltaSupplier = Objects.requireNonNull(frameDeltaSupplier, "frameDeltaSupplier");
    }

    MenuInputSnapshot sample() {
        MenuCommand keyboard = Objects.requireNonNullElse(keyboardSource.get(), MenuCommand.idle());
        ControllerFrame current = Objects.requireNonNullElse(
            controllerSource.poll(),
            ControllerFrame.disconnected()
        );
        MenuCommand controller = controllerCommand(current, previousController, safeFrameDelta());
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

    private MenuCommand controllerCommand(ControllerFrame current, ControllerFrame previous, float delta) {
        if (!current.connected()) {
            navigationRepeater.reset();
            return MenuCommand.idle();
        }

        boolean upHeld = current.dpadUp() || current.leftStick().y() < -STICK_NAVIGATION_THRESHOLD;
        boolean downHeld = current.dpadDown() || current.leftStick().y() > STICK_NAVIGATION_THRESHOLD;
        boolean leftHeld = current.dpadLeft() || current.leftStick().x() < -STICK_NAVIGATION_THRESHOLD;
        boolean rightHeld = current.dpadRight() || current.leftStick().x() > STICK_NAVIGATION_THRESHOLD;
        NavigationRepeater.Directions directions = navigationRepeater.update(
            upHeld,
            downHeld,
            leftHeld,
            rightHeld,
            delta
        );

        return new MenuCommand(
            directions.up(),
            directions.down(),
            directions.left(),
            directions.right(),
            justPressed(current.buttonA(), previous.buttonA())
                || justPressed(current.buttonStart(), previous.buttonStart()),
            justPressed(current.buttonB(), previous.buttonB())
                || justPressed(current.buttonBack(), previous.buttonBack())
        );
    }

    private float safeFrameDelta() {
        double value = frameDeltaSupplier.getAsDouble();
        if (!Double.isFinite(value) || value <= 0d) return 0f;
        return (float) Math.min(value, 0.10d);
    }

    private static double currentFrameDelta() {
        if (Gdx.graphics == null) return 1d / 60d;
        return Gdx.graphics.getDeltaTime();
    }

    private static boolean anyJustPressed(int first, int second) {
        return Gdx.input.isKeyJustPressed(first) || Gdx.input.isKeyJustPressed(second);
    }

    private static boolean justPressed(boolean current, boolean previous) {
        return current && !previous;
    }
}
