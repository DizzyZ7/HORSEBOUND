// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Pure-Java visual Gate transition state; persistent truth remains PlacedStructure.isOpen(). */
final class GateAnimationState {
    static final float OPEN_ANGLE = 90f;
    static final float DEGREES_PER_SECOND = 210f;

    private final Map<UUID, Float> offsets = new HashMap<>();

    float update(UUID structureId, boolean open, float deltaSeconds) {
        if (structureId == null) return open ? OPEN_ANGLE : 0f;
        float target = open ? OPEN_ANGLE : 0f;
        Float current = offsets.get(structureId);
        if (current == null) {
            offsets.put(structureId, target);
            return target;
        }
        float delta = safeDelta(deltaSeconds);
        float maximumChange = DEGREES_PER_SECOND * delta;
        float next = moveTowards(current, target, maximumChange);
        offsets.put(structureId, next);
        return next;
    }

    void retain(Set<UUID> aliveGateIds) {
        if (aliveGateIds == null || aliveGateIds.isEmpty()) {
            offsets.clear();
            return;
        }
        offsets.keySet().removeIf(id -> !aliveGateIds.contains(id));
    }

    int trackedCount() {
        return offsets.size();
    }

    private static float moveTowards(float value, float target, float maximumChange) {
        if (value < target) return Math.min(target, value + maximumChange);
        if (value > target) return Math.max(target, value - maximumChange);
        return target;
    }

    private static float safeDelta(float value) {
        if (!Float.isFinite(value) || value <= 0f) return 0f;
        return Math.min(value, FixedStepClock.DEFAULT_MAX_FRAME_SECONDS);
    }
}
