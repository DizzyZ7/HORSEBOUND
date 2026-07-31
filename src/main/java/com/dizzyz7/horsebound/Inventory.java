// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.EnumMap;
import java.util.Map;

final class Inventory {
    private final EnumMap<ItemId, Integer> amounts = new EnumMap<>(ItemId.class);

    Inventory() {
        for (ItemId item : ItemId.values()) {
            amounts.put(item, 0);
        }
    }

    static Inventory starter(int wood, int apples) {
        Inventory inventory = new Inventory();
        inventory.set(ItemId.WOOD, wood);
        inventory.set(ItemId.APPLE, apples);
        return inventory;
    }

    int count(ItemId item) {
        return amounts.getOrDefault(item, 0);
    }

    int add(ItemId item, int amount) {
        if (amount <= 0) return 0;
        int current = count(item);
        int accepted = Math.min(amount, item.stackLimit() - current);
        amounts.put(item, current + accepted);
        return accepted;
    }

    boolean remove(ItemId item, int amount) {
        if (amount <= 0) return true;
        int current = count(item);
        if (current < amount) return false;
        amounts.put(item, current - amount);
        return true;
    }

    void set(ItemId item, int amount) {
        amounts.put(item, Math.max(0, Math.min(item.stackLimit(), amount)));
    }

    Map<ItemId, Integer> snapshot() {
        return Map.copyOf(amounts);
    }
}
