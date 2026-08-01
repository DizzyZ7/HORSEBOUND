// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchAmbienceTest {
    @Test
    void dayAndNightSelectDifferentAmbienceCues() {
        RanchAmbience ambience = new RanchAmbience(42L);

        assertEquals(RanchAudio.Cue.MEADOW_BREEZE, ambience.cueFor(0.50f));
        assertEquals(RanchAudio.Cue.NIGHT_CRICKETS, ambience.cueFor(0.05f));
        assertEquals(RanchAudio.Cue.NIGHT_CRICKETS, ambience.cueFor(0.90f));
    }

    @Test
    void intervalSequenceIsDeterministicAndBounded() {
        RanchAmbience first = new RanchAmbience(77L);
        RanchAmbience second = new RanchAmbience(77L);

        for (int i = 0; i < 8; i++) {
            float interval = first.nextIntervalSeconds();
            assertEquals(interval, second.nextIntervalSeconds());
            assertTrue(interval >= 7.5f);
            assertTrue(interval <= 12f);
        }
    }

    @Test
    void invalidOrNonPositiveDeltaDoesNotAdvanceCountdown() {
        RanchAmbience ambience = new RanchAmbience(1L);
        float before = ambience.secondsUntilNext();

        ambience.update(Float.NaN, 0.5f);
        ambience.update(-1f, 0.5f);

        assertEquals(before, ambience.secondsUntilNext());
    }
}
