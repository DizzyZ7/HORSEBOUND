// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PushikMindTest {
    @Test
    void followsWhenFarAndSleepsNearPlayerAtNight() {
        PushikMind mind = new PushikMind();
        mind.tick(1f, 12f, 0.5f);
        assertEquals(PushikState.FOLLOW, mind.state());

        mind.tick(1f, 2f, 0.90f);
        assertEquals(PushikState.SLEEP, mind.state());
    }

    @Test
    void pettingRaisesAffectionAndTriggersGreeting() {
        PushikMind mind = new PushikMind();
        float before = mind.affection();
        mind.pet();
        assertTrue(mind.affection() > before);
        assertEquals(PushikState.GREET, mind.state());
    }
}
