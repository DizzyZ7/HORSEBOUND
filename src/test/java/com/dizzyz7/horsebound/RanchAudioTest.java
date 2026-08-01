// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchAudioTest {
    @AfterEach
    void cleanup() {
        RanchAudio.shutdown();
    }

    @Test
    void everyCueProducesFiniteBoundedMonoSamples() {
        for (RanchAudio.Cue cue : RanchAudio.Cue.values()) {
            float[] samples = RanchAudio.synthesize(cue);
            assertEquals(
                Math.round(cue.durationSeconds() * RanchAudio.SAMPLE_RATE),
                samples.length
            );
            float peak = 0f;
            for (float sample : samples) {
                assertTrue(Float.isFinite(sample));
                assertTrue(sample >= -1f && sample <= 1f);
                peak = Math.max(peak, Math.abs(sample));
            }
            assertTrue(peak > 0.01f, cue + " should not be silent");
        }
    }

    @Test
    void synthesisIsDeterministicAndEndsNearSilence() {
        float[] first = RanchAudio.synthesize(RanchAudio.Cue.GATE_OPEN);
        float[] second = RanchAudio.synthesize(RanchAudio.Cue.GATE_OPEN);

        assertEquals(first.length, second.length);
        for (int i = 0; i < first.length; i++) assertEquals(first[i], second[i]);
        assertTrue(Math.abs(first[0]) < 0.0001f);
        assertTrue(Math.abs(first[first.length - 1]) < 0.0001f);
    }
}
