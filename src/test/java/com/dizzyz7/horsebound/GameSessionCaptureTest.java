// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSessionCaptureTest {
    @AfterEach
    void cleanup() {
        GameSessionCapture.cancel();
    }

    @Test
    void capturesOnlyTheSessionConstructedInsideScope() {
        new GameSession(SaveGame.fresh(new WorldSeed(1L)));

        GameSessionCapture.begin();
        GameSession expected = new GameSession(SaveGame.fresh(new WorldSeed(2L)));

        assertSame(expected, GameSessionCapture.finish());
    }

    @Test
    void missingConstructionFailsInsteadOfReturningStaleSession() {
        GameSessionCapture.begin();
        assertThrows(IllegalStateException.class, GameSessionCapture::finish);
    }
}
