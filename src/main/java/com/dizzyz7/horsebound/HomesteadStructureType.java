// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Map;
import java.util.Optional;

enum HomesteadStructureType {
    FENCE("Fence", Map.of(ItemId.WOOD, 2), 0, null, 0, 0, 1.25f),
    GATE("Gate", Map.of(ItemId.WOOD, 4), 0, null, 0, 0, 1.40f),
    FEEDER("Feeder", Map.of(ItemId.WOOD, 6), 20, ItemId.HAY, 5, 0, 0.90f),
    WATER_TROUGH("Water Trough", Map.of(ItemId.WOOD, 4, ItemId.STONE, 4), 20, ItemId.WATER_BUCKET, 5, 0, 1.10f),
    HAY_STORAGE("Hay Storage", Map.of(ItemId.WOOD, 8), 100, ItemId.HAY, 5, 0, 1.45f),
    CHEST("Chest", Map.of(ItemId.WOOD, 8), 0, null, 0, 12, 0.80f),
    STALL("Stable Stall", Map.of(ItemId.WOOD, 20, ItemId.STONE, 8), 0, null, 0, 0, 1.90f);

    private final String displayName;
    private final Map<ItemId, Integer> buildCost;
    private final int storageCapacity;
    private final ItemId acceptedResource;
    private final int resourceUnitsPerItem;
    private final int itemStorageSlots;
    private final float collisionRadius;

    HomesteadStructureType(
        String displayName,
        Map<ItemId, Integer> buildCost,
        int storageCapacity,
        ItemId acceptedResource,
        int resourceUnitsPerItem,
        int itemStorageSlots,
        float collisionRadius
    ) {
        this.displayName = displayName;
        this.buildCost = Map.copyOf(buildCost);
        this.storageCapacity = Math.max(0, storageCapacity);
        this.acceptedResource = acceptedResource;
        this.resourceUnitsPerItem = Math.max(0, resourceUnitsPerItem);
        this.itemStorageSlots = Math.max(0, itemStorageSlots);
        this.collisionRadius = Float.isFinite(collisionRadius) ? Math.max(0f, collisionRadius) : 0f;
    }

    String displayName() {
        return displayName;
    }

    Map<ItemId, Integer> buildCost() {
        return buildCost;
    }

    int storageCapacity() {
        return storageCapacity;
    }

    ItemId acceptedResource() {
        return acceptedResource;
    }

    int resourceUnitsPerItem() {
        return resourceUnitsPerItem;
    }

    int itemStorageSlots() {
        return itemStorageSlots;
    }

    float collisionRadius() {
        return collisionRadius;
    }

    boolean storesResource() {
        return acceptedResource != null && storageCapacity > 0 && resourceUnitsPerItem > 0;
    }

    boolean storesItems() {
        return itemStorageSlots > 0;
    }

    boolean canToggleOpen() {
        return this == GATE;
    }

    static Optional<HomesteadStructureType> parse(String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            return Optional.of(valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
