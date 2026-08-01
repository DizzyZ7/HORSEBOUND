// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Render-thread context controlling which legacy gameplay actions are owned by Homestead UX. */
final class HomesteadInputContext {
    private static boolean active;
    private static boolean captureInteract;
    private static boolean capturePauseAsCancel;

    private HomesteadInputContext() {
    }

    static void configure(boolean enabled, boolean interact, boolean pauseAsCancel) {
        active = enabled;
        captureInteract = enabled && interact;
        capturePauseAsCancel = enabled && pauseAsCancel;
    }

    static boolean capturesBuild() {
        return active;
    }

    static boolean capturesInteract() {
        return captureInteract;
    }

    static boolean capturesPauseAsCancel() {
        return capturePauseAsCancel;
    }

    static void reset() {
        active = false;
        captureInteract = false;
        capturePauseAsCancel = false;
    }
}
