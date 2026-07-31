// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;

/**
 * Rendering-thread adapter over the official gdx-controllers standardized mapping.
 */
final class GdxControllerStateSource implements ControllerStateSource {
    private static final float MOVE_DEAD_ZONE = 0.20f;
    private static final float LOOK_DEAD_ZONE = 0.16f;

    private Controller activeController;

    @Override
    public ControllerFrame poll() {
        Controller controller = resolveController();
        if (controller == null) {
            return ControllerFrame.disconnected();
        }

        ControllerMapping mapping = controller.getMapping();
        AnalogStick left = ControllerDeadZone.radial(
            safeAxis(controller, mapping.axisLeftX),
            safeAxis(controller, mapping.axisLeftY),
            MOVE_DEAD_ZONE
        );
        AnalogStick right = ControllerDeadZone.radial(
            safeAxis(controller, mapping.axisRightX),
            safeAxis(controller, mapping.axisRightY),
            LOOK_DEAD_ZONE
        );

        return new ControllerFrame(
            true,
            left,
            right,
            safeButton(controller, mapping.buttonA),
            safeButton(controller, mapping.buttonB),
            safeButton(controller, mapping.buttonX),
            safeButton(controller, mapping.buttonY),
            safeButton(controller, mapping.buttonBack),
            safeButton(controller, mapping.buttonStart),
            safeButton(controller, mapping.buttonL1),
            safeButton(controller, mapping.buttonR1),
            safeButton(controller, mapping.buttonDpadUp),
            safeButton(controller, mapping.buttonDpadDown),
            safeButton(controller, mapping.buttonDpadLeft),
            safeButton(controller, mapping.buttonDpadRight)
        );
    }

    private Controller resolveController() {
        if (activeController != null && activeController.isConnected()) {
            return activeController;
        }

        Controller current = Controllers.getCurrent();
        if (current != null && current.isConnected()) {
            activeController = current;
            return activeController;
        }

        Array<Controller> controllers = Controllers.getControllers();
        for (Controller controller : controllers) {
            if (controller != null && controller.isConnected()) {
                activeController = controller;
                return activeController;
            }
        }

        activeController = null;
        return null;
    }

    private static float safeAxis(Controller controller, int axisCode) {
        if (axisCode == ControllerMapping.UNDEFINED || axisCode < 0 || axisCode >= controller.getAxisCount()) {
            return 0f;
        }
        float value = controller.getAxis(axisCode);
        return Float.isFinite(value) ? Math.max(-1f, Math.min(1f, value)) : 0f;
    }

    private static boolean safeButton(Controller controller, int buttonCode) {
        if (buttonCode == ControllerMapping.UNDEFINED) return false;
        if (buttonCode < controller.getMinButtonIndex() || buttonCode > controller.getMaxButtonIndex()) {
            return false;
        }
        return controller.getButton(buttonCode);
    }
}
