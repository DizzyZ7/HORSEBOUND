// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;

/** Best-effort haptic feedback. Unsupported devices fail silently and never affect gameplay. */
final class ControllerRumble {
    private ControllerRumble() {
    }

    static void pulse(InputProfile profile, int durationMillis, float intensityScale) {
        if (profile == null || !profile.rumbleEnabled()) return;
        Controller controller = currentController();
        if (controller == null || !controller.canVibrate()) return;
        int duration = Math.max(15, Math.min(500, durationMillis));
        float strength = Math.max(0f, Math.min(1f, profile.rumbleStrength() * intensityScale));
        try {
            controller.startVibration(duration, strength);
        } catch (RuntimeException ignored) {
            // Rumble is optional. Driver/backend failures must never break input or gameplay.
        }
    }

    private static Controller currentController() {
        try {
            Controller current = Controllers.getCurrent();
            if (current != null && current.isConnected()) return current;
            Array<Controller> controllers = Controllers.getControllers();
            for (Controller controller : controllers) {
                if (controller != null && controller.isConnected()) return controller;
            }
        } catch (RuntimeException ignored) {
            // Headless tests and unsupported runtimes safely behave as no-rumble devices.
        }
        return null;
    }
}
