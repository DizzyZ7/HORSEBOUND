// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Map;
import java.util.Optional;

enum HomesteadStructureType {
    FENCE("Fence", Map.of(ItemId.WOOD, 2), 0, null, 0),
    GATE("Gate", Map.of(ItemId.WOOD, 4), 0, null, 0),
    FEEDER("Feeder", Map.of(ItemId.WOOD, 6), 20, ItemId.HAY, 5),
    WATER_TROUGH("Water Trough", Map.of(ItemId.WOOD, 4, ItemId.STONE, 4), 20, ItemId.WATER_BUCKET, 5),
    HAY_STORAGE("Hay Storage", Map.of(ItemId.WOOD, 8), 100, ItemId.HAY, 5),
    CHEST("Chest", Map.of(ItemId.WOOD, 8), 60, null, 0),
    STALL("Stable Stall", Map.of(ItemId.WOOD, 20, ItemId.STONE, 8), 0, null, 0);

    private final String displayName;
    private final Map<ItemId, Integer> buildCost;
    private final int storageCapacity;
    private final ItemId acceptedResource;
    private final int resourceUnitsPerItem;

    HomesteadStructureType(
        String displayName,
        Map<ItemId, Integer> buildCost,
        int storageCapacity,
        ItemId acceptedResource,
        int resourceUnitsPerItem
    ) {
        this.displayName = displayName;
        this.buildCost = Map.copyOf(buildCost);
        this.storageCapacity = Math.max(0, storageCapacity);
        this.acceptedResource = acceptedResource;
        this.resourceUnitsPerItem = Math.max(0, resourceUnitsPerItem);
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

    boolean storesResource() {
        return acceptedResource != null && storageCapacity > 0 && resourceUnitsPerItem > 0;
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
