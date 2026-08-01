// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;
import java.util.UUID;

/** Session-local two-step confirmation for destructive ranch dismantling. */
final class RanchDismantleConfirmation {
    static final float DEFAULT_WINDOW_SECONDS = 4f;

    private final float windowSeconds;
    private UUID structureId;
    private PlacedStructure armedStructure;
    private long operationalVersion;
    private float remainingSeconds;

    RanchDismantleConfirmation() {
        this(DEFAULT_WINDOW_SECONDS);
    }

    RanchDismantleConfirmation(float windowSeconds) {
        this.windowSeconds = Float.isFinite(windowSeconds)
            ? Math.max(0.5f, windowSeconds)
            : DEFAULT_WINDOW_SECONDS;
    }

    Decision request(PlacedStructure structure) {
        if (structure == null) {
            cancel();
            return Decision.INVALID;
        }
        if (isArmedFor(structure)) {
            cancel();
            return Decision.CONFIRMED;
        }
        structureId = Objects.requireNonNull(structure.id(), "structure id");
        armedStructure = structure;
        operationalVersion = structure.operationalVersion();
        remainingSeconds = windowSeconds;
        return Decision.ARMED;
    }

    void tick(float deltaSeconds) {
        if (!isArmed()) return;
        if (!Float.isFinite(deltaSeconds) || deltaSeconds <= 0f) return;
        remainingSeconds = Math.max(0f, remainingSeconds - deltaSeconds);
        if (remainingSeconds <= 0f) cancel();
    }

    boolean isArmed() {
        if (structureId == null || armedStructure == null || remainingSeconds <= 0f) return false;
        if (operationalVersion != armedStructure.operationalVersion()) {
            cancel();
            return false;
        }
        return true;
    }

    boolean isArmedFor(PlacedStructure structure) {
        return structure != null
            && isArmed()
            && structureId.equals(structure.id())
            && operationalVersion == structure.operationalVersion();
    }

    UUID structureId() {
        return structureId;
    }

    float remainingSeconds() {
        return isArmed() ? remainingSeconds : 0f;
    }

    void cancel() {
        structureId = null;
        armedStructure = null;
        operationalVersion = 0L;
        remainingSeconds = 0f;
    }

    enum Decision {
        ARMED,
        CONFIRMED,
        INVALID
    }
}
