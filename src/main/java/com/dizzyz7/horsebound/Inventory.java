// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class Inventory {
    private static final int MAX_AMOUNT_PER_ITEM = 1_000_000;

    private final EnumMap<ItemId, Integer> amounts = new EnumMap<>(ItemId.class);

    Inventory() {
        for (ItemId item : ItemId.values()) amounts.put(item, 0);
    }

    static Inventory starter(int wood, int apples) {
        Inventory inventory = new Inventory();
        inventory.set(ItemId.WOOD, wood);
        inventory.set(ItemId.APPLE, apples);
        inventory.set(ItemId.HAY, 6);
        inventory.set(ItemId.WATER_BUCKET, 2);
        return inventory;
    }

    static Inventory restore(List<SaveGame.ItemStackData> items, int legacyWood, int legacyApples) {
        Inventory inventory = starter(legacyWood, legacyApples);
        if (items == null || items.isEmpty()) return inventory;

        for (ItemId item : ItemId.values()) inventory.set(item, 0);
        for (SaveGame.ItemStackData saved : items) {
            if (saved == null) continue;
            ItemId.parse(saved.itemId()).ifPresent(item -> inventory.add(item, saved.amount()));
        }
        return inventory;
    }

    int count(ItemId item) {
        return amounts.getOrDefault(Objects.requireNonNull(item, "item"), 0);
    }

    int add(ItemId item, int amount) {
        Objects.requireNonNull(item, "item");
        if (amount <= 0) return 0;
        int current = count(item);
        int accepted = Math.min(amount, MAX_AMOUNT_PER_ITEM - current);
        amounts.put(item, current + accepted);
        return accepted;
    }

    boolean remove(ItemId item, int amount) {
        Objects.requireNonNull(item, "item");
        if (amount <= 0) return true;
        int current = count(item);
        if (current < amount) return false;
        amounts.put(item, current - amount);
        return true;
    }

    boolean has(ItemId item, int amount) {
        return amount <= 0 || count(item) >= amount;
    }

    void set(ItemId item, int amount) {
        Objects.requireNonNull(item, "item");
        amounts.put(item, Math.max(0, Math.min(MAX_AMOUNT_PER_ITEM, amount)));
    }

    Map<ItemId, Integer> snapshot() {
        return Map.copyOf(amounts);
    }

    List<InventoryStack> stackView() {
        List<InventoryStack> result = new ArrayList<>();
        for (ItemId item : ItemId.values()) {
            int remaining = count(item);
            while (remaining > 0) {
                int stack = Math.min(remaining, item.stackLimit());
                result.add(new InventoryStack(item, stack));
                remaining -= stack;
            }
        }
        return List.copyOf(result);
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
