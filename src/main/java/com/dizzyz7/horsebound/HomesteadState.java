// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

final class HomesteadState {
    private final List<PlacedStructure> structures = new ArrayList<>();

    HomesteadState() {
    }

    static HomesteadState restore(List<SaveGame.StructureData> savedStructures) {
        HomesteadState state = new HomesteadState();
        if (savedStructures == null) return state;
        Set<UUID> seenIds = new HashSet<>();
        for (SaveGame.StructureData saved : savedStructures) {
            if (saved == null || !seenIds.add(saved.id())) continue;
            state.structures.add(new PlacedStructure(
                saved.id(),
                saved.type(),
                saved.x(),
                saved.z(),
                saved.heading(),
                saved.storedUnits(),
                saved.open(),
                saved.storedItems()
            ));
        }
        return state;
    }

    Optional<PlacedStructure> place(
        HomesteadStructureType type,
        float x,
        float z,
        float heading,
        Inventory inventory
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(inventory, "inventory");
        for (Map.Entry<ItemId, Integer> cost : type.buildCost().entrySet()) {
            if (!inventory.has(cost.getKey(), cost.getValue())) return Optional.empty();
        }
        for (Map.Entry<ItemId, Integer> cost : type.buildCost().entrySet()) {
            inventory.remove(cost.getKey(), cost.getValue());
        }
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (find(id).isPresent());
        PlacedStructure placed = new PlacedStructure(id, type, x, z, heading, 0);
        structures.add(placed);
        return Optional.of(placed);
    }

    boolean relocate(UUID id, float x, float z, float heading) {
        PlacedStructure structure = find(id).orElse(null);
        return structure != null && structure.relocate(x, z, heading);
    }

    boolean toggleGate(UUID id) {
        PlacedStructure structure = find(id).orElse(null);
        return structure != null && structure.toggleOpen();
    }

    DismantleResult dismantle(UUID id, Inventory refundTo) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(refundTo, "refundTo");
        PlacedStructure structure = find(id).orElse(null);
        if (structure == null) return DismantleResult.NOT_FOUND;
        if (structure.storedUnits() > 0 || !structure.itemStorage().isEmpty()) {
            return DismantleResult.STORAGE_NOT_EMPTY;
        }

        List<SaveGame.ItemStackData> refund = new ArrayList<>();
        for (Map.Entry<ItemId, Integer> cost : structure.type().buildCost().entrySet()) {
            refund.add(new SaveGame.ItemStackData(cost.getKey().name(), Math.max(1, cost.getValue() / 2)));
        }
        if (!refundTo.canAccept(refund)) return DismantleResult.INVENTORY_FULL;

        structures.remove(structure);
        for (SaveGame.ItemStackData item : refund) {
            ItemId.parse(item.itemId()).ifPresent(idValue -> refundTo.add(idValue, item.amount()));
        }
        return DismantleResult.SUCCESS;
    }

    /** Compatibility alias retained for older tests and call sites. */
    boolean remove(UUID id, Inventory refundTo) {
        if (refundTo == null) {
            return structures.removeIf(structure -> structure.id().equals(id));
        }
        return dismantle(id, refundTo) == DismantleResult.SUCCESS;
    }

    int depositOneResource(UUID structureId, Inventory inventory) {
        Objects.requireNonNull(structureId, "structureId");
        Objects.requireNonNull(inventory, "inventory");
        PlacedStructure structure = find(structureId).orElse(null);
        if (structure == null || !structure.type().storesResource()) return 0;
        ItemId resource = structure.type().acceptedResource();
        int units = structure.type().resourceUnitsPerItem();
        if (structure.type().storageCapacity() - structure.storedUnits() < units) return 0;
        if (!inventory.remove(resource, 1)) return 0;
        int accepted = structure.addUnits(units);
        if (accepted != units) {
            inventory.add(resource, 1);
            return 0;
        }
        return accepted;
    }

    TransferResult storeOneItem(UUID structureId, Inventory source, ItemId item) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(item, "item");
        PlacedStructure structure = find(structureId).orElse(null);
        if (structure == null) return TransferResult.NOT_FOUND;
        if (!structure.type().storesItems()) return TransferResult.NOT_ITEM_STORAGE;
        if (!source.has(item, 1)) return TransferResult.NO_ITEM;
        if (structure.itemStorage().availableSpace(item) < 1) return TransferResult.FULL;
        return source.transferTo(structure.itemStorage(), item, 1)
            ? TransferResult.SUCCESS
            : TransferResult.FULL;
    }

    TransferResult takeOneItem(UUID structureId, Inventory destination, ItemId item) {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(item, "item");
        PlacedStructure structure = find(structureId).orElse(null);
        if (structure == null) return TransferResult.NOT_FOUND;
        if (!structure.type().storesItems()) return TransferResult.NOT_ITEM_STORAGE;
        if (!structure.itemStorage().has(item, 1)) return TransferResult.NO_ITEM;
        if (destination.availableSpace(item) < 1) return TransferResult.FULL;
        return structure.itemStorage().transferTo(destination, item, 1)
            ? TransferResult.SUCCESS
            : TransferResult.FULL;
    }

    boolean consumeNearestResource(ItemId resource, float x, float z, float radius) {
        Objects.requireNonNull(resource, "resource");
        float safeRadius = safeRadius(radius);
        PlacedStructure nearest = null;
        float bestDistanceSquared = safeRadius * safeRadius;
        for (PlacedStructure structure : structures) {
            if (structure.type().acceptedResource() != resource || structure.storedUnits() <= 0) continue;
            float distanceSquared = distanceSquared(x, z, structure.x(), structure.z());
            if (distanceSquared > bestDistanceSquared) continue;
            bestDistanceSquared = distanceSquared;
            nearest = structure;
        }
        return nearest != null && nearest.consumeUnit();
    }

    boolean hasNearby(HomesteadStructureType type, float x, float z, float radius) {
        Objects.requireNonNull(type, "type");
        float safeRadius = safeRadius(radius);
        float radiusSquared = safeRadius * safeRadius;
        for (PlacedStructure structure : structures) {
            if (structure.type() == type && distanceSquared(x, z, structure.x(), structure.z()) <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    Optional<PlacedStructure> nearest(float x, float z, float radius) {
        float safeRadius = safeRadius(radius);
        PlacedStructure nearest = null;
        float bestDistanceSquared = safeRadius * safeRadius;
        for (PlacedStructure structure : structures) {
            float distanceSquared = distanceSquared(x, z, structure.x(), structure.z());
            if (distanceSquared > bestDistanceSquared) continue;
            bestDistanceSquared = distanceSquared;
            nearest = structure;
        }
        return Optional.ofNullable(nearest);
    }

    Optional<PlacedStructure> find(UUID id) {
        if (id == null) return Optional.empty();
        return structures.stream().filter(value -> value.id().equals(id)).findFirst();
    }

    List<PlacedStructure> structures() {
        return List.copyOf(structures);
    }

    List<SaveGame.StructureData> toSaveData() {
        return structures.stream().map(PlacedStructure::toSaveData).toList();
    }

    private static float safeRadius(float value) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(0f, value);
    }

    private static float distanceSquared(float ax, float az, float bx, float bz) {
        float safeAx = Float.isFinite(ax) ? ax : 0f;
        float safeAz = Float.isFinite(az) ? az : 0f;
        float dx = safeAx - bx;
        float dz = safeAz - bz;
        return dx * dx + dz * dz;
    }

    enum TransferResult {
        SUCCESS,
        NOT_FOUND,
        NOT_ITEM_STORAGE,
        NO_ITEM,
        FULL
    }

    enum DismantleResult {
        SUCCESS,
        NOT_FOUND,
        STORAGE_NOT_EMPTY,
        INVENTORY_FULL
    }
}
