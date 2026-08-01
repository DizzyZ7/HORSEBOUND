// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PauseRequestBusTest {
    @AfterEach
    void cleanUp() {
        PauseRequestBus.reset();
    }

    @Test
    void requestIsDeliveredExactlyOnce() {
        assertFalse(PauseRequestBus.consume());
        PauseRequestBus.request();
        assertTrue(PauseRequestBus.consume());
        assertFalse(PauseRequestBus.consume());
    }
}
