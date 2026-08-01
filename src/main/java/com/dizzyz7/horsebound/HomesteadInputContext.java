// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Render-thread context controlling which legacy gameplay actions are owned by Homestead UX. */
final class HomesteadInputContext {
    private static boolean active;
    private static boolean captureInteract;
    private static boolean captureMount;
    private static boolean captureInventory;
    private static boolean capturePauseAsCancel;

    private HomesteadInputContext() {
    }

    static void configure(boolean enabled, boolean interact, boolean mount, boolean inventory, boolean pauseAsCancel) {
        active = enabled;
        captureInteract = enabled && interact;
        captureMount = enabled && mount;
        captureInventory = enabled && inventory;
        capturePauseAsCancel = enabled && pauseAsCancel;
    }

    /** Compatibility overload for 0.5.1 call sites. */
    static void configure(boolean enabled, boolean interact, boolean pauseAsCancel) {
        configure(enabled, interact, false, enabled, pauseAsCancel);
    }

    static boolean capturesBuild() {
        return active;
    }

    static boolean capturesInteract() {
        return captureInteract;
    }

    static boolean capturesMount() {
        return captureMount;
    }

    static boolean capturesInventory() {
        return captureInventory;
    }

    static boolean capturesPauseAsCancel() {
        return capturePauseAsCancel;
    }

    static void reset() {
        active = false;
        captureInteract = false;
        captureMount = false;
        captureInventory = false;
        capturePauseAsCancel = false;
    }
}
