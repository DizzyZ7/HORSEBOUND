// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

@FunctionalInterface
interface ControllerStateSource {
    ControllerFrame poll();
}
