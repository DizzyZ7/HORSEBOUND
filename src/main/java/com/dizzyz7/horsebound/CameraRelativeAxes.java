// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Pure camera-relative planar basis used by keyboard and controller movement. */
final class CameraRelativeAxes {
    private CameraRelativeAxes() {
    }

    static float rightX(float forwardZ) {
        return -forwardZ;
    }

    static float rightZ(float forwardX) {
        return forwardX;
    }
}
