// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

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
}
