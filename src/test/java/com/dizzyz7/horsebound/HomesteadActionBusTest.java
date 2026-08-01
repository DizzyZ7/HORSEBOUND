// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomesteadActionBusTest {
    @AfterEach
    void cleanup() {
        HomesteadActionBus.reset();
        HomesteadInputContext.reset();
    }

    @Test
    void actionsAreDeliveredExactlyOnce() {
        HomesteadActionBus.requestBuild();
        HomesteadActionBus.requestInteract();
        HomesteadActionBus.requestCancel();

        assertTrue(HomesteadActionBus.consumeBuild());
        assertFalse(HomesteadActionBus.consumeBuild());
        assertTrue(HomesteadActionBus.consumeInteract());
        assertFalse(HomesteadActionBus.consumeInteract());
        assertTrue(HomesteadActionBus.consumeCancel());
        assertFalse(HomesteadActionBus.consumeCancel());
    }

    @Test
    void contextCapturesOnlyConfiguredActions() {
        HomesteadInputContext.configure(true, false, true);
        assertTrue(HomesteadInputContext.capturesBuild());
        assertFalse(HomesteadInputContext.capturesInteract());
        assertTrue(HomesteadInputContext.capturesPauseAsCancel());
    }
}
