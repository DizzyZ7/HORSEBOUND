// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Repeats held menu directions after a deliberate delay while action buttons remain edge-only. */
final class NavigationRepeater {
    static final float INITIAL_DELAY_SECONDS = 0.38f;
    static final float REPEAT_INTERVAL_SECONDS = 0.105f;

    private final RepeatKey up = new RepeatKey();
    private final RepeatKey down = new RepeatKey();
    private final RepeatKey left = new RepeatKey();
    private final RepeatKey right = new RepeatKey();

    Directions update(boolean upHeld, boolean downHeld, boolean leftHeld, boolean rightHeld, float deltaSeconds) {
        float delta = safeDelta(deltaSeconds);
        return new Directions(
            up.update(upHeld, delta),
            down.update(downHeld, delta),
            left.update(leftHeld, delta),
            right.update(rightHeld, delta)
        );
    }

    void reset() {
        up.reset();
        down.reset();
        left.reset();
        right.reset();
    }

    private static float safeDelta(float value) {
        if (!Float.isFinite(value) || value <= 0f) return 0f;
        return Math.min(value, 0.10f);
    }

    record Directions(boolean up, boolean down, boolean left, boolean right) {
    }

    private static final class RepeatKey {
        private boolean held;
        private float remaining;

        private boolean update(boolean current, float delta) {
            if (!current) {
                reset();
                return false;
            }
            if (!held) {
                held = true;
                remaining = INITIAL_DELAY_SECONDS;
                return true;
            }
            remaining -= delta;
            if (remaining > 0f) return false;
            remaining += REPEAT_INTERVAL_SECONDS;
            return true;
        }

        private void reset() {
            held = false;
            remaining = 0f;
        }
    }
}
