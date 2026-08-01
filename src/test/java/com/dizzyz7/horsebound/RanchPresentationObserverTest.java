// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchPresentationObserverTest {
    @Test
    void initialSnapshotIsSilentAndLaterChangesEmitSemanticCues() {
        RanchPresentationObserver observer = new RanchPresentationObserver();
        PlacedStructure gate = new PlacedStructure(
            UUID.randomUUID(), HomesteadStructureType.GATE, 2f, 3f, 0f, 0
        );

        assertTrue(observer.observe(List.of(gate)).isEmpty());

        gate.toggleOpen();
        assertEquals(List.of(RanchAudio.Cue.GATE_OPEN), observer.observe(List.of(gate)));

        gate.relocate(5f, 6f, 45f);
        assertEquals(List.of(RanchAudio.Cue.MOVE), observer.observe(List.of(gate)));

        assertEquals(List.of(RanchAudio.Cue.DISMANTLE), observer.observe(List.of()));
    }

    @Test
    void newlyPlacedStructureEmitsBuildWithoutFalseMoveOrGateCue() {
        RanchPresentationObserver observer = new RanchPresentationObserver();
        observer.observe(List.of());
        PlacedStructure openGate = new PlacedStructure(
            UUID.randomUUID(),
            HomesteadStructureType.GATE,
            4f,
            5f,
            90f,
            0,
            true,
            List.of()
        );

        assertEquals(List.of(RanchAudio.Cue.BUILD), observer.observe(List.of(openGate)));
        assertEquals(1, observer.trackedCount());
    }
}
