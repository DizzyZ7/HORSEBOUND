// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchAudioTest {
    @AfterEach
    void cleanup() {
        RanchAudio.setVolumes(GameSettings.DEFAULT_SFX_VOLUME, GameSettings.DEFAULT_AMBIENCE_VOLUME);
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

    @Test
    void busVolumeScalesWithoutMutatingCachedWaveform() {
        float[] source = {1f, -0.5f, 0.25f};
        float[] half = RanchAudio.applyBusVolume(source, 0.5f, GameSettings.DEFAULT_SFX_VOLUME);

        assertNotSame(source, half);
        assertEquals(0.5f, half[0]);
        assertEquals(-0.25f, half[1]);
        assertEquals(0.125f, half[2]);
        assertEquals(1f, source[0]);
        assertSame(source, RanchAudio.applyBusVolume(source, 1f, GameSettings.DEFAULT_SFX_VOLUME));
    }

    @Test
    void busesNormalizeIndependently() {
        RanchAudio.setVolumes(-2f, 4f);
        assertEquals(0f, RanchAudio.sfxVolume());
        assertEquals(1f, RanchAudio.ambienceVolume());
        RanchAudio.setVolumes(Float.NaN, Float.NaN);
        assertEquals(GameSettings.DEFAULT_SFX_VOLUME, RanchAudio.sfxVolume());
        assertEquals(GameSettings.DEFAULT_AMBIENCE_VOLUME, RanchAudio.ambienceVolume());
    }

    @Test
    void authoredAmbienceAndInteractionCuesUseSeparateBuses() {
        assertEquals(RanchAudio.Bus.SFX, RanchAudio.Cue.BUILD.bus());
        assertEquals(RanchAudio.Bus.AMBIENCE, RanchAudio.Cue.MEADOW_BREEZE.bus());
        assertEquals(RanchAudio.Bus.AMBIENCE, RanchAudio.Cue.NIGHT_CRICKETS.bus());
    }
}
