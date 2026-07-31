// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

/**
 * Decouples domain simulation cadence from rendering cadence.
 * Large frame stalls are capped and the number of catch-up steps is bounded to avoid a spiral of death.
 */
final class FixedStepClock {
    static final float DEFAULT_STEP_SECONDS = 1f / 60f;
    static final float DEFAULT_MAX_FRAME_SECONDS = 0.25f;
    static final int DEFAULT_MAX_STEPS_PER_FRAME = 15;

    private static final double STEP_EPSILON_RATIO = 1e-4;

    private final double stepSeconds;
    private final double maxFrameSeconds;
    private final int maxStepsPerFrame;
    private final double epsilon;
    private double accumulator;

    FixedStepClock() {
        this(DEFAULT_STEP_SECONDS, DEFAULT_MAX_FRAME_SECONDS, DEFAULT_MAX_STEPS_PER_FRAME);
    }

    FixedStepClock(float stepSeconds, float maxFrameSeconds, int maxStepsPerFrame) {
        if (!Float.isFinite(stepSeconds) || stepSeconds <= 0f) {
            throw new IllegalArgumentException("stepSeconds must be finite and positive");
        }
        if (!Float.isFinite(maxFrameSeconds) || maxFrameSeconds < stepSeconds) {
            throw new IllegalArgumentException("maxFrameSeconds must be finite and at least one step");
        }
        if (maxStepsPerFrame < 1) {
            throw new IllegalArgumentException("maxStepsPerFrame must be positive");
        }
        this.stepSeconds = stepSeconds;
        this.maxFrameSeconds = maxFrameSeconds;
        this.maxStepsPerFrame = maxStepsPerFrame;
        this.epsilon = stepSeconds * STEP_EPSILON_RATIO;
    }

    int advance(float frameDeltaSeconds, SimulationStep simulationStep) {
        Objects.requireNonNull(simulationStep, "simulationStep");
        if (!Float.isFinite(frameDeltaSeconds) || frameDeltaSeconds <= 0f) {
            return 0;
        }

        accumulator += Math.min(frameDeltaSeconds, maxFrameSeconds);
        int steps = 0;
        while (accumulator + epsilon >= stepSeconds && steps < maxStepsPerFrame) {
            simulationStep.update((float) stepSeconds);
            accumulator -= stepSeconds;
            steps++;
        }

        if (steps == maxStepsPerFrame && accumulator + epsilon >= stepSeconds) {
            accumulator %= stepSeconds;
        }
        if (accumulator < 0d && accumulator > -epsilon) {
            accumulator = 0d;
        }
        return steps;
    }

    float interpolationAlpha() {
        return (float) Math.max(0d, Math.min(1d, accumulator / stepSeconds));
    }

    float stepSeconds() {
        return (float) stepSeconds;
    }
}
