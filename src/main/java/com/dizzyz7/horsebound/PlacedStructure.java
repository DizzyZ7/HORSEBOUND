// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;
import java.util.UUID;

final class PlacedStructure {
    private final UUID id;
    private final HomesteadStructureType type;
    private final float x;
    private final float z;
    private final float heading;
    private int storedUnits;

    PlacedStructure(
        UUID id,
        HomesteadStructureType type,
        float x,
        float z,
        float heading,
        int storedUnits
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.x = finiteOrZero(x);
        this.z = finiteOrZero(z);
        this.heading = finiteOrZero(heading);
        this.storedUnits = Math.max(0, Math.min(type.storageCapacity(), storedUnits));
    }

    UUID id() {
        return id;
    }

    HomesteadStructureType type() {
        return type;
    }

    float x() {
        return x;
    }

    float z() {
        return z;
    }

    float heading() {
        return heading;
    }

    int storedUnits() {
        return storedUnits;
    }

    int addUnits(int amount) {
        if (!type.storesResource() || amount <= 0) return 0;
        int accepted = Math.min(amount, type.storageCapacity() - storedUnits);
        storedUnits += accepted;
        return accepted;
    }

    boolean consumeUnit() {
        if (storedUnits <= 0) return false;
        storedUnits--;
        return true;
    }

    SaveGame.StructureData toSaveData() {
        return new SaveGame.StructureData(id, type, x, z, heading, storedUnits);
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0f;
    }
}
