// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

record SaveSlotInfo(
    String slotId,
    String label,
    State state,
    long savedAtEpochMillis,
    long worldSeed,
    int horseCount,
    int tamedHorseCount,
    int structureCount
) {
    enum State {
        EMPTY,
        READY,
        CORRUPT
    }

    boolean canLoad() {
        return state == State.READY;
    }
}
