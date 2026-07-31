// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

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
}
