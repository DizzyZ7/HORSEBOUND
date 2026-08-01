// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Optional;

enum ItemId {
    WOOD("Wood", 99),
    STONE("Stone", 99),
    APPLE("Apple", 20),
    CARROT("Carrot", 20),
    HAY("Hay", 50),
    WATER_BUCKET("Water Bucket", 10);

    private final String displayName;
    private final int stackLimit;

    ItemId(String displayName, int stackLimit) {
        this.displayName = displayName;
        this.stackLimit = stackLimit;
    }

    String displayName() {
        return displayName;
    }

    /** Maximum amount displayed in one inventory/hotbar stack, not the total owned amount. */
    int stackLimit() {
        return stackLimit;
    }

    static Optional<ItemId> parse(String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            return Optional.of(ItemId.valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
