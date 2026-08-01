// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

record InventoryStack(ItemId item, int amount) {
    InventoryStack {
        item = Objects.requireNonNull(item, "item");
        if (amount <= 0 || amount > item.stackLimit()) {
            throw new IllegalArgumentException("Invalid " + item + " stack amount: " + amount);
        }
    }
}
