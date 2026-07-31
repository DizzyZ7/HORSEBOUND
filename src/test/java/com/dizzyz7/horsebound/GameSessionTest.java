// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {
    @Test
    void restoresInventoryAndAdvancesWorldClockThroughFixedSimulationTicks() {
        SaveGame save = SaveGame.fresh(new WorldSeed(12345L));
        GameSession session = new GameSession(save);

        assertEquals(12345L, session.worldSeed());
        assertEquals(4, session.inventory().count(ItemId.WOOD));
        assertEquals(5, session.inventory().count(ItemId.APPLE));

        float before = session.worldTime();
        simulate(session, 60, 12);

        assertEquals(720, session.simulationTicks());
        assertEquals(before + 0.01f, session.worldTime(), 0.0001f);
        assertTrue(session.simulationInterpolationAlpha() >= 0f);
        assertTrue(session.simulationInterpolationAlpha() <= 1f);
    }

    @Test
    void worldClockProducesSameResultAtThirtySixtyAndOneHundredFortyFourFps() {
        GameSession at30 = new GameSession(SaveGame.fresh(new WorldSeed(1L)));
        GameSession at60 = new GameSession(SaveGame.fresh(new WorldSeed(1L)));
        GameSession at144 = new GameSession(SaveGame.fresh(new WorldSeed(1L)));

        simulate(at30, 30, 10);
        simulate(at60, 60, 10);
        simulate(at144, 144, 10);

        assertEquals(600, at30.simulationTicks());
        assertEquals(at30.simulationTicks(), at60.simulationTicks());
        assertEquals(at30.simulationTicks(), at144.simulationTicks());
        assertEquals(at30.worldTime(), at60.worldTime(), 0.000001f);
        assertEquals(at30.worldTime(), at144.worldTime(), 0.000001f);
    }

    @Test
    void restoresTypedInventoryAndPushikMindInsteadOfLegacyDefaults() {
        SaveGame save = new SaveGame(
            SaveGame.CURRENT_VERSION,
            77L,
            1000L,
            0.4f,
            new SaveGame.PlayerData(
                0f,
                0f,
                0f,
                1,
                1,
                List.of(
                    new SaveGame.ItemStackData(ItemId.WOOD.name(), 19),
                    new SaveGame.ItemStackData(ItemId.APPLE.name(), 12)
                )
            ),
            new SaveGame.PushikData(2f, 3f, 45f, 88f, PushikState.SIT),
            List.of(),
            List.of(),
            List.of()
        );

        GameSession session = new GameSession(save);

        assertEquals(19, session.inventory().count(ItemId.WOOD));
        assertEquals(12, session.inventory().count(ItemId.APPLE));
        assertEquals(88f, session.pushikMind().affection());
        assertEquals(PushikState.SIT, session.pushikMind().state());
    }

    private static void simulate(GameSession session, int framesPerSecond, int seconds) {
        float frameDelta = 1f / framesPerSecond;
        for (int i = 0; i < framesPerSecond * seconds; i++) {
            session.advanceWorldTime(frameDelta);
        }
    }
}
