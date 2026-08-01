// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

final class PlacedStructure {
    private final UUID id;
    private final HomesteadStructureType type;
    private final Inventory itemStorage;
    private float x;
    private float z;
    private float heading;
    private int storedUnits;
    private boolean open;

    PlacedStructure(
        UUID id,
        HomesteadStructureType type,
        float x,
        float z,
        float heading,
        int storedUnits
    ) {
        this(id, type, x, z, heading, storedUnits, false, List.of());
    }

    PlacedStructure(
        UUID id,
        HomesteadStructureType type,
        float x,
        float z,
        float heading,
        int storedUnits,
        boolean open,
        List<SaveGame.ItemStackData> storedItems
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.x = finiteOrZero(x);
        this.z = finiteOrZero(z);
        this.heading = normalizeHeading(heading);
        this.storedUnits = Math.max(0, Math.min(type.storageCapacity(), storedUnits));
        this.open = type.canToggleOpen() && open;
        this.itemStorage = type.storesItems()
            ? Inventory.restore(storedItems, 0, 0, type.itemStorageSlots())
            : new Inventory(1);
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

    boolean isOpen() {
        return open;
    }

    boolean blocksMovement() {
        return !(type == HomesteadStructureType.GATE && open);
    }

    Inventory itemStorage() {
        return itemStorage;
    }

    boolean relocate(float nextX, float nextZ, float nextHeading) {
        if (!Float.isFinite(nextX) || !Float.isFinite(nextZ) || !Float.isFinite(nextHeading)) return false;
        x = nextX;
        z = nextZ;
        heading = normalizeHeading(nextHeading);
        return true;
    }

    boolean toggleOpen() {
        if (!type.canToggleOpen()) return false;
        open = !open;
        return true;
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

    int recoverResourceItems(Inventory destination) {
        Objects.requireNonNull(destination, "destination");
        if (!type.storesResource() || storedUnits <= 0) return 0;
        int unitsPerItem = type.resourceUnitsPerItem();
        int recoverableItems = storedUnits / unitsPerItem;
        int accepted = destination.add(type.acceptedResource(), recoverableItems);
        storedUnits -= accepted * unitsPerItem;
        return accepted;
    }

    SaveGame.StructureData toSaveData() {
        return new SaveGame.StructureData(
            id,
            type,
            x,
            z,
            heading,
            storedUnits,
            open,
            itemStorage.toSaveData()
        );
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0f;
    }

    private static float normalizeHeading(float value) {
        if (!Float.isFinite(value)) return 0f;
        float normalized = value % 360f;
        return normalized < 0f ? normalized + 360f : normalized;
    }
}
