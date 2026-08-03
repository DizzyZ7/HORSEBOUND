// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraRelativeAxesTest {
    @Test
    void rightAxisIsTheVisibleScreenRightForCardinalCameraDirections() {
        assertRight(0f, 1f, -1f, 0f);
        assertRight(1f, 0f, 0f, 1f);
        assertRight(0f, -1f, 1f, 0f);
        assertRight(-1f, 0f, 0f, -1f);
    }

    private static void assertRight(float forwardX, float forwardZ, float expectedX, float expectedZ) {
        assertEquals(expectedX, CameraRelativeAxes.rightX(forwardZ), 0.0001f);
        assertEquals(expectedZ, CameraRelativeAxes.rightZ(forwardX), 0.0001f);
    }
}
