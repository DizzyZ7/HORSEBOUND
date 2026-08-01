// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** One-shot render-thread bridge from the existing device-neutral command stream to Homestead UX. */
final class HomesteadActionBus {
    private static boolean buildRequested;
    private static boolean interactRequested;
    private static boolean inventoryRequested;
    private static boolean dismantleRequested;
    private static boolean cancelRequested;

    private HomesteadActionBus() {
    }

    static void requestBuild() {
        buildRequested = true;
    }

    static void requestInteract() {
        interactRequested = true;
    }

    static void requestInventory() {
        inventoryRequested = true;
    }

    static void requestDismantle() {
        dismantleRequested = true;
    }

    static void requestCancel() {
        cancelRequested = true;
    }

    static boolean consumeBuild() {
        boolean result = buildRequested;
        buildRequested = false;
        return result;
    }

    static boolean consumeInteract() {
        boolean result = interactRequested;
        interactRequested = false;
        return result;
    }

    static boolean consumeInventory() {
        boolean result = inventoryRequested;
        inventoryRequested = false;
        return result;
    }

    static boolean consumeDismantle() {
        boolean result = dismantleRequested;
        dismantleRequested = false;
        return result;
    }

    static boolean consumeCancel() {
        boolean result = cancelRequested;
        cancelRequested = false;
        return result;
    }

    static void reset() {
        buildRequested = false;
        interactRequested = false;
        inventoryRequested = false;
        dismantleRequested = false;
        cancelRequested = false;
    }
}
