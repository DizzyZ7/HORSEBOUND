// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.concurrent.atomic.AtomicBoolean;

/** Transfers a semantic gameplay pause request to the application screen coordinator. */
final class PauseRequestBus {
    private static final AtomicBoolean REQUESTED = new AtomicBoolean();

    private PauseRequestBus() {
    }

    static void request() {
        REQUESTED.set(true);
    }

    static boolean consume() {
        return REQUESTED.getAndSet(false);
    }

    static void reset() {
        REQUESTED.set(false);
    }
}
