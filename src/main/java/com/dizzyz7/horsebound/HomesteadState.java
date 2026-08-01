// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

final class HomesteadState {
    private final List<PlacedStructure> structures = new ArrayList<>();

    HomesteadState() {
    }

    static HomesteadState restore(List<SaveGame.StructureData> savedStructures) {
        HomesteadState state = new HomesteadState();
        if (savedStructures == null) return state;
        for (SaveGame.StructureData saved : savedStructures) {
            if (saved == null) continue;
            state.structures.add(new PlacedStructure(
                saved.id(),
                saved.type(),
                saved.x(),
                saved.z(),
                saved.heading(),
                saved.storedUnits()
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
        PlacedStructure placed = new PlacedStructure(UUID.randomUUID(), type, x, z, heading, 0);
        structures.add(placed);
        return Optional.of(placed);
    }

    boolean remove(UUID id, Inventory refundTo) {
        Objects.requireNonNull(id, "id");
        for (int i = 0; i < structures.size(); i++) {
            PlacedStructure structure = structures.get(i);
            if (!structure.id().equals(id)) continue;
            structures.remove(i);
            if (refundTo != null) {
                for (Map.Entry<ItemId, Integer> cost : structure.type().buildCost().entrySet()) {
                    refundTo.add(cost.getKey(), Math.max(1, cost.getValue() / 2));
                }
            }
            return true;
        }
        return false;
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

    boolean consumeNearestResource(ItemId resource, float x, float z, float radius) {
        PlacedStructure nearest = null;
        float bestDistanceSquared = radius * radius;
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
        float radiusSquared = radius * radius;
        for (PlacedStructure structure : structures) {
            if (structure.type() == type && distanceSquared(x, z, structure.x(), structure.z()) <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    Optional<PlacedStructure> find(UUID id) {
        return structures.stream().filter(value -> value.id().equals(id)).findFirst();
    }

    List<PlacedStructure> structures() {
        return List.copyOf(structures);
    }

    List<SaveGame.StructureData> toSaveData() {
        return structures.stream().map(PlacedStructure::toSaveData).toList();
    }

    private static float distanceSquared(float ax, float az, float bx, float bz) {
        float dx = ax - bx;
        float dz = az - bz;
        return dx * dx + dz * dz;
    }
}
