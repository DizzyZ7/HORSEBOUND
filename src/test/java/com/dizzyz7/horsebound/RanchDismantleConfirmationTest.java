// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchDismantleConfirmationTest {
    @Test
    void sameUnchangedStructureRequiresTwoRequests() {
        PlacedStructure structure = structure(UUID.randomUUID());
        RanchDismantleConfirmation confirmation = new RanchDismantleConfirmation(3f);

        assertEquals(RanchDismantleConfirmation.Decision.ARMED, confirmation.request(structure));
        assertTrue(confirmation.isArmedFor(structure));
        assertEquals(RanchDismantleConfirmation.Decision.CONFIRMED, confirmation.request(structure));
        assertFalse(confirmation.isArmed());
    }

    @Test
    void expiryRequiresFreshConfirmation() {
        PlacedStructure structure = structure(UUID.randomUUID());
        RanchDismantleConfirmation confirmation = new RanchDismantleConfirmation(1f);
        confirmation.request(structure);

        confirmation.tick(1.1f);

        assertFalse(confirmation.isArmed());
        assertEquals(RanchDismantleConfirmation.Decision.ARMED, confirmation.request(structure));
    }

    @Test
    void operationalChangeInvalidatesTheArmedRequest() {
        PlacedStructure gate = new PlacedStructure(
            UUID.randomUUID(), HomesteadStructureType.GATE, 1f, 2f, 0f, 0
        );
        RanchDismantleConfirmation confirmation = new RanchDismantleConfirmation();
        confirmation.request(gate);
        gate.toggleOpen();

        assertFalse(confirmation.isArmed());
        assertEquals(RanchDismantleConfirmation.Decision.ARMED, confirmation.request(gate));
        assertTrue(confirmation.isArmedFor(gate));
    }

    @Test
    void selectingAnotherStructureRearmsInsteadOfConfirming() {
        RanchDismantleConfirmation confirmation = new RanchDismantleConfirmation();
        PlacedStructure first = structure(UUID.randomUUID());
        PlacedStructure second = structure(UUID.randomUUID());
        confirmation.request(first);

        assertEquals(RanchDismantleConfirmation.Decision.ARMED, confirmation.request(second));
        assertEquals(second.id(), confirmation.structureId());
    }

    private static PlacedStructure structure(UUID id) {
        return new PlacedStructure(id, HomesteadStructureType.FENCE, 1f, 2f, 0f, 0);
    }
}
