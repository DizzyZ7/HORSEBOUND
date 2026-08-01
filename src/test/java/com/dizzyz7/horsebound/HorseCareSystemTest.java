// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HorseCareSystemTest {
    @Test
    void nearbyServicesAutomaticallyFeedWaterAndRestHorse() {
        SaveGame.StructureData feeder = new SaveGame.StructureData(
            UUID.randomUUID(), HomesteadStructureType.FEEDER, 0f, 0f, 0f, 3
        );
        SaveGame.StructureData trough = new SaveGame.StructureData(
            UUID.randomUUID(), HomesteadStructureType.WATER_TROUGH, 1f, 0f, 0f, 3
        );
        SaveGame.StructureData stall = new SaveGame.StructureData(
            UUID.randomUUID(), HomesteadStructureType.STALL, 2f, 0f, 0f, 0
        );
        HomesteadState homestead = HomesteadState.restore(List.of(feeder, trough, stall));

        HorseCareSystem.CareResult result = new HorseCareSystem().update(
            new HorseNeeds(10f, 12f, 15f),
            1f,
            false,
            false,
            0f,
            0f,
            homestead
        );

        assertTrue(result.fed());
        assertTrue(result.watered());
        assertTrue(result.rested());
        assertTrue(result.needs().hunger() > 40f);
        assertTrue(result.needs().thirst() > 50f);
        assertTrue(result.needs().energy() > 15f);
        assertEquals(2, homestead.find(feeder.id()).orElseThrow().storedUnits());
        assertEquals(2, homestead.find(trough.id()).orElseThrow().storedUnits());
    }

    @Test
    void distantOrEmptyServicesDoNotFakeCare() {
        HomesteadState homestead = HomesteadState.restore(List.of(
            new SaveGame.StructureData(
                UUID.randomUUID(), HomesteadStructureType.FEEDER, 100f, 100f, 0f, 0
            )
        ));

        HorseCareSystem.CareResult result = new HorseCareSystem().update(
            new HorseNeeds(20f, 20f, 20f),
            10f,
            true,
            true,
            0f,
            0f,
            homestead
        );

        assertFalse(result.fed());
        assertFalse(result.watered());
        assertFalse(result.rested());
        assertTrue(result.needs().hunger() < 20f);
        assertTrue(result.needs().thirst() < 20f);
        assertTrue(result.needs().energy() < 20f);
    }
}
