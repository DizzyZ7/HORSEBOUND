// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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

    static Inventory restore(List<SaveGame.ItemStackData> items, int legacyWood, int legacyApples) {
        Inventory inventory = starter(legacyWood, legacyApples);
        if (items == null || items.isEmpty()) return inventory;

        for (ItemId item : ItemId.values()) inventory.set(item, 0);
        for (SaveGame.ItemStackData saved : items) {
            ItemId.parse(saved.itemId()).ifPresent(item -> inventory.set(item, saved.amount()));
        }
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

    List<SaveGame.ItemStackData> toSaveData() {
        List<SaveGame.ItemStackData> result = new ArrayList<>();
        for (ItemId item : ItemId.values()) {
            int amount = count(item);
            if (amount > 0) result.add(new SaveGame.ItemStackData(item.name(), amount));
        }
        return List.copyOf(result);
    }
}
