// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedStepClockTest {
    @Test
    void producesSameSimulationTicksAtThirtySixtyAndOneHundredFortyFourFps() {
        assertEquals(600, simulateFrames(30, 10));
        assertEquals(600, simulateFrames(60, 10));
        assertEquals(600, simulateFrames(144, 10));
    }

    @Test
    void capsLargeFrameStallsAndBoundsCatchUpWork() {
        FixedStepClock clock = new FixedStepClock();
        AtomicInteger ticks = new AtomicInteger();

        int steps = clock.advance(12f, ignored -> ticks.incrementAndGet());

        assertEquals(FixedStepClock.DEFAULT_MAX_STEPS_PER_FRAME, steps);
        assertEquals(FixedStepClock.DEFAULT_MAX_STEPS_PER_FRAME, ticks.get());
        assertTrue(clock.interpolationAlpha() >= 0f && clock.interpolationAlpha() <= 1f);
    }

    @Test
    void exposesInterpolationAlphaBetweenFixedUpdates() {
        FixedStepClock clock = new FixedStepClock();

        assertEquals(0, clock.advance(clock.stepSeconds() * 0.5f, ignored -> { }));
        assertEquals(0.5f, clock.interpolationAlpha(), 0.0001f);

        assertEquals(1, clock.advance(clock.stepSeconds() * 0.5f, ignored -> { }));
        assertEquals(0f, clock.interpolationAlpha(), 0.0001f);
    }

    private static int simulateFrames(int framesPerSecond, int seconds) {
        FixedStepClock clock = new FixedStepClock();
        AtomicInteger ticks = new AtomicInteger();
        float frameDelta = 1f / framesPerSecond;
        for (int i = 0; i < framesPerSecond * seconds; i++) {
            clock.advance(frameDelta, ignored -> ticks.incrementAndGet());
        }
        return ticks.get();
    }
}
