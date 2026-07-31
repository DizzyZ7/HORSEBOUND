// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSessionTest {
    @Test
    void restoresInventoryAndAdvancesWorldClock() {
        SaveGame save = SaveGame.fresh(new WorldSeed(12345L));
        GameSession session = new GameSession(save);

        assertEquals(12345L, session.worldSeed());
        assertEquals(4, session.inventory().count(ItemId.WOOD));
        assertEquals(5, session.inventory().count(ItemId.APPLE));

        float before = session.worldTime();
        session.advanceWorldTime(12f);
        assertEquals(before + 0.01f, session.worldTime(), 0.0001f);
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
}
