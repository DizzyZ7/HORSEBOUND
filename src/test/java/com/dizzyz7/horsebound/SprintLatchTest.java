// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SprintLatchTest {
    @Test
    void holdModeFollowsPhysicalButton() {
        SprintLatch latch = new SprintLatch();
        assertFalse(latch.update(false, false, SprintMode.HOLD));
        assertTrue(latch.update(true, true, SprintMode.HOLD));
        assertTrue(latch.update(true, false, SprintMode.HOLD));
        assertFalse(latch.update(false, false, SprintMode.HOLD));
    }

    @Test
    void toggleModeChangesOnlyOnPressEdges() {
        SprintLatch latch = new SprintLatch();
        assertTrue(latch.update(true, true, SprintMode.TOGGLE));
        assertTrue(latch.update(true, false, SprintMode.TOGGLE));
        assertTrue(latch.update(false, false, SprintMode.TOGGLE));
        assertFalse(latch.update(true, true, SprintMode.TOGGLE));
    }

    @Test
    void changingModeClearsLatchedState() {
        SprintLatch latch = new SprintLatch();
        assertTrue(latch.update(true, true, SprintMode.TOGGLE));
        assertFalse(latch.update(false, false, SprintMode.HOLD));
        assertFalse(latch.update(false, false, SprintMode.TOGGLE));
    }
}
