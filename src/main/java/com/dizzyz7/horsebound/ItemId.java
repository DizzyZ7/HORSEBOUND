// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Optional;

enum ItemId {
    WOOD("Wood", 99),
    APPLE("Apple", 20);

    private final String displayName;
    private final int stackLimit;

    ItemId(String displayName, int stackLimit) {
        this.displayName = displayName;
        this.stackLimit = stackLimit;
    }

    String displayName() {
        return displayName;
    }

    int stackLimit() {
        return stackLimit;
    }

    static Optional<ItemId> parse(String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            return Optional.of(ItemId.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
