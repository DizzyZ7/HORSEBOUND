// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveSlotOverwriteConfirmationTest {
    @Test
    void sameContinuouslySelectedSlotRequiresTwoRequests() {
        SaveSlotOverwriteConfirmation confirmation = new SaveSlotOverwriteConfirmation();

        assertEquals(SaveSlotOverwriteConfirmation.Decision.ARMED, confirmation.request("slot-1"));
        assertTrue(confirmation.isArmedFor("slot-1"));
        assertEquals(SaveSlotOverwriteConfirmation.Decision.CONFIRMED, confirmation.request("slot-1"));
        assertFalse(confirmation.isArmedFor("slot-1"));
    }

    @Test
    void leavingTheSlotCancelsTheDestructiveConfirmation() {
        SaveSlotOverwriteConfirmation confirmation = new SaveSlotOverwriteConfirmation();
        confirmation.request("slot-1");

        confirmation.selectionChanged("slot-2");

        assertFalse(confirmation.isArmedFor("slot-1"));
        assertEquals(SaveSlotOverwriteConfirmation.Decision.ARMED, confirmation.request("slot-1"));
    }

    @Test
    void invalidSlotCannotRemainArmed() {
        SaveSlotOverwriteConfirmation confirmation = new SaveSlotOverwriteConfirmation();
        confirmation.request("slot-1");

        assertEquals(SaveSlotOverwriteConfirmation.Decision.INVALID, confirmation.request("  "));
        assertFalse(confirmation.isArmedFor("slot-1"));
    }
}
