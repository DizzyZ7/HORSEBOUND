// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Pure-Java sprint state used by keyboard and controller adapters. */
final class SprintLatch {
    private SprintMode previousMode = SprintMode.HOLD;
    private boolean toggled;

    boolean update(boolean pressed, boolean justPressed, SprintMode mode) {
        SprintMode safeMode = mode == null ? SprintMode.HOLD : mode;
        if (safeMode != previousMode) {
            toggled = false;
            previousMode = safeMode;
        }
        if (safeMode == SprintMode.HOLD) return pressed;
        if (justPressed) toggled = !toggled;
        return toggled;
    }

    void reset() {
        toggled = false;
        previousMode = SprintMode.HOLD;
    }
}
