// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSessionHomesteadTest {
    @Test
    void restoresV4InventoryHotbarAndPlacedStructures() {
        UUID feederId = UUID.fromString("ef13acd1-4982-47e7-908d-dc92db754fd2");
        SaveGame save = new SaveGame(
            SaveGame.CURRENT_VERSION,
            77L,
            1000L,
            0.4f,
            new SaveGame.PlayerData(
                0f,
                0f,
                0f,
                120,
                0,
                List.of(new SaveGame.ItemStackData(ItemId.WOOD.name(), 120))
            ),
            new SaveGame.PushikData(2f, 3f, 45f, 88f, PushikState.SIT),
            List.of(),
            List.of(),
            List.of(new SaveGame.StructureData(
                feederId,
                HomesteadStructureType.FEEDER,
                5f,
                6f,
                90f,
                11
            )),
            new SaveGame.HotbarData(
                3,
                List.of("WOOD", "HAY", "WATER_BUCKET", "APPLE", "", "", "", "")
            ),
            List.of()
        );

        GameSession session = new GameSession(save);

        assertEquals(120, session.inventory().count(ItemId.WOOD));
        assertEquals(3, session.hotbar().selectedIndex());
        assertEquals(ItemId.APPLE, session.hotbar().selectedItem());
        assertEquals(11, session.homestead().find(feederId).orElseThrow().storedUnits());
    }
}
