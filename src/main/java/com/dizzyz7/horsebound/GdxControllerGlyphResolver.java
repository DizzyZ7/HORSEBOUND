// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;

/** Render-thread adapter that resolves the family of the currently connected controller. */
final class GdxControllerGlyphResolver {
    private GdxControllerGlyphResolver() {
    }

    static ControllerGlyphFamily currentFamily() {
        Controller current = Controllers.getCurrent();
        if (current != null && current.isConnected()) {
            return ControllerGlyphFamily.fromControllerName(current.getName());
        }
        Array<Controller> controllers = Controllers.getControllers();
        for (Controller controller : controllers) {
            if (controller != null && controller.isConnected()) {
                return ControllerGlyphFamily.fromControllerName(controller.getName());
            }
        }
        return ControllerGlyphFamily.GENERIC;
    }
}
