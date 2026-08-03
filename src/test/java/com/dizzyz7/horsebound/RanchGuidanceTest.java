// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RanchGuidanceTest {
    @Test
    void guidanceFollowsTheActualFirstRanchLoop() {
        GameSession session = new GameSession(SaveGame.fresh(new WorldSeed(77L)));
        assertObjective(session, List.of(), "guidance.gather_wood");

        session.inventory().add(ItemId.WOOD, 20);
        assertObjective(session, List.of(), "guidance.build_feeder");

        PlacedStructure feeder = session.homestead()
            .place(HomesteadStructureType.FEEDER, 2f, 2f, 0f, session.inventory())
            .orElseThrow();
        assertObjective(session, List.of(), "guidance.stock_feeder");

        session.homestead().depositOneResource(feeder.id(), session.inventory());
        assertObjective(session, List.of(), "guidance.build_trough");

        PlacedStructure trough = session.homestead()
            .place(HomesteadStructureType.WATER_TROUGH, 6f, 2f, 0f, session.inventory())
            .orElseThrow();
        assertObjective(session, List.of(), "guidance.fill_trough");

        session.homestead().depositOneResource(trough.id(), session.inventory());
        UUID horseId = UUID.randomUUID();
        assertObjective(
            session,
            List.of(new RanchWorldAccess.HorseTelemetry(horseId, 0f, 0f, 0f, false, false)),
            "guidance.befriend_horse"
        );
        assertObjective(
            session,
            List.of(new RanchWorldAccess.HorseTelemetry(horseId, 0f, 0f, 0f, false, true)),
            "guidance.mount_horse"
        );
        assertObjective(
            session,
            List.of(new RanchWorldAccess.HorseTelemetry(horseId, 0f, 0f, 0f, true, true)),
            "guidance.complete"
        );
    }

    private static void assertObjective(
        GameSession session,
        List<RanchWorldAccess.HorseTelemetry> horses,
        String expected
    ) {
        assertEquals(expected, RanchGuidance.next(session, horses).titleKey());
    }
}
