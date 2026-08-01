// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Session-local two-step guard for replacing an occupied ranch slot. */
final class SaveSlotOverwriteConfirmation {
    private String armedSlotId;

    Decision request(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            clear();
            return Decision.INVALID;
        }
        String normalized = slotId.trim();
        if (normalized.equals(armedSlotId)) {
            clear();
            return Decision.CONFIRMED;
        }
        armedSlotId = normalized;
        return Decision.ARMED;
    }

    void selectionChanged(String selectedSlotId) {
        if (armedSlotId == null) return;
        if (selectedSlotId == null || !armedSlotId.equals(selectedSlotId)) clear();
    }

    boolean isArmedFor(String slotId) {
        return armedSlotId != null && armedSlotId.equals(slotId);
    }

    void clear() {
        armedSlotId = null;
    }

    enum Decision {
        ARMED,
        CONFIRMED,
        INVALID
    }
}
