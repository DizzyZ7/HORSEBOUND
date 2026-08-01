// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Deterministic, low-frequency ambience scheduler over the shared procedural audio engine. */
final class RanchAmbience {
    private final long seed;
    private long sequence;
    private float secondsUntilNext = 2.5f;

    RanchAmbience(long seed) {
        this.seed = seed;
    }

    void update(float deltaSeconds, float worldTime) {
        if (!Float.isFinite(deltaSeconds) || deltaSeconds <= 0f) return;
        secondsUntilNext -= Math.min(deltaSeconds, FixedStepClock.DEFAULT_MAX_FRAME_SECONDS);
        if (secondsUntilNext > 0f) return;
        RanchAudio.play(cueFor(worldTime));
        secondsUntilNext = nextIntervalSeconds();
    }

    RanchAudio.Cue cueFor(float worldTime) {
        float phase = normalizedDay(worldTime);
        return phase < 0.22f || phase > 0.78f
            ? RanchAudio.Cue.NIGHT_CRICKETS
            : RanchAudio.Cue.MEADOW_BREEZE;
    }

    float nextIntervalSeconds() {
        long mixed = seed ^ (++sequence * 0x9E3779B97F4A7C15L);
        mixed ^= mixed >>> 30;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94D049BB133111EBL;
        mixed ^= mixed >>> 31;
        float unit = (mixed >>> 40) / (float) (1 << 24);
        return 7.5f + unit * 4.5f;
    }

    float secondsUntilNext() {
        return Math.max(0f, secondsUntilNext);
    }

    private static float normalizedDay(float worldTime) {
        if (!Float.isFinite(worldTime)) return 0.5f;
        float phase = worldTime - (float) Math.floor(worldTime);
        return phase < 0f ? phase + 1f : phase;
    }
}
