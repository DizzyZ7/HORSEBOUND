// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameMetricsTest {
    @Test
    void sixtyFpsMeetsDeckTarget() {
        FrameMetrics metrics = new FrameMetrics();
        for (int i = 0; i < 120; i++) metrics.record(1f / 60f);

        assertEquals(60, metrics.averageFps());
        assertEquals(16.666f, metrics.averageMilliseconds(), 0.02f);
        assertTrue(metrics.meetsDeckTarget());
    }

    @Test
    void twentyFpsFailsDeckTarget() {
        FrameMetrics metrics = new FrameMetrics();
        for (int i = 0; i < 120; i++) metrics.record(1f / 20f);

        assertEquals(20, metrics.averageFps());
        assertFalse(metrics.meetsDeckTarget());
    }

    @Test
    void rollingWindowForgetsOldSpikes() {
        FrameMetrics metrics = new FrameMetrics();
        metrics.record(0.5f);
        for (int i = 0; i < 120; i++) metrics.record(1f / 60f);

        assertTrue(metrics.worstMilliseconds() < 20f);
    }

    @Test
    void invalidSamplesAreIgnored() {
        FrameMetrics metrics = new FrameMetrics();
        metrics.record(Float.NaN);
        metrics.record(-1f);

        assertEquals(0, metrics.averageFps());
        assertEquals(0f, metrics.averageMilliseconds());
    }
}
