// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Optional;

enum ItemId {
    WOOD("item.wood", "item.short.wood", 99),
    STONE("item.stone", "item.short.stone", 99),
    APPLE("item.apple", "item.short.apple", 20),
    CARROT("item.carrot", "item.short.carrot", 20),
    HAY("item.hay", "item.short.hay", 50),
    WATER_BUCKET("item.water_bucket", "item.short.water_bucket", 10);

    private final String displayKey;
    private final String shortKey;
    private final int stackLimit;

    ItemId(String displayKey, String shortKey, int stackLimit) {
        this.displayKey = displayKey;
        this.shortKey = shortKey;
        this.stackLimit = stackLimit;
    }

    String displayName() {
        return I18n.text(displayKey);
    }

    String shortName() {
        return I18n.text(shortKey);
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
