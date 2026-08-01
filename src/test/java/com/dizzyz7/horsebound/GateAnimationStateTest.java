// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GateAnimationStateTest {
    @Test
    void restoredGateStartsAtPersistentTargetThenAnimatesChanges() {
        GateAnimationState state = new GateAnimationState();
        UUID id = UUID.randomUUID();

        assertEquals(0f, state.update(id, false, 1f / 60f));
        float opening = state.update(id, true, 1f / 60f);
        assertTrue(opening > 0f && opening < GateAnimationState.OPEN_ANGLE);

        float value = opening;
        for (int i = 0; i < 60; i++) value = state.update(id, true, 1f / 60f);
        assertEquals(GateAnimationState.OPEN_ANGLE, value);

        float closing = state.update(id, false, 1f / 60f);
        assertTrue(closing < GateAnimationState.OPEN_ANGLE);
    }

    @Test
    void staleGateAnimationStateIsReleased() {
        GateAnimationState state = new GateAnimationState();
        UUID id = UUID.randomUUID();
        state.update(id, true, 0f);
        assertEquals(1, state.trackedCount());

        state.retain(Set.of());
        assertEquals(0, state.trackedCount());
    }
}
