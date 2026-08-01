// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class Inventory {
    static final int DEFAULT_SLOT_CAPACITY = 24;
    private static final int MAX_AMOUNT_PER_ITEM = 1_000_000;

    private final EnumMap<ItemId, Integer> amounts = new EnumMap<>(ItemId.class);
    private final int slotCapacity;
    private long mutationVersion;

    Inventory() {
        this(DEFAULT_SLOT_CAPACITY);
    }

    Inventory(int slotCapacity) {
        this.slotCapacity = Math.max(1, slotCapacity);
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
        return restore(items, legacyWood, legacyApples, DEFAULT_SLOT_CAPACITY);
    }

    static Inventory restore(
        List<SaveGame.ItemStackData> items,
        int legacyWood,
        int legacyApples,
        int slotCapacity
    ) {
        Inventory inventory = new Inventory(slotCapacity);
        inventory.set(ItemId.WOOD, legacyWood);
        inventory.set(ItemId.APPLE, legacyApples);
        if (items == null || items.isEmpty()) return inventory;

        inventory.clear();
        for (SaveGame.ItemStackData saved : items) {
            if (saved == null) continue;
            ItemId.parse(saved.itemId()).ifPresent(item -> {
                long restored = (long) inventory.count(item) + saved.amount();
                inventory.set(item, (int) Math.min(MAX_AMOUNT_PER_ITEM, restored));
            });
        }
        return inventory;
    }

    Inventory copy() {
        Inventory copy = new Inventory(slotCapacity);
        for (ItemId item : ItemId.values()) copy.set(item, count(item));
        return copy;
    }

    int count(ItemId item) {
        return amounts.getOrDefault(Objects.requireNonNull(item, "item"), 0);
    }

    int add(ItemId item, int amount) {
        Objects.requireNonNull(item, "item");
        if (amount <= 0) return 0;
        int accepted = Math.min(amount, availableSpace(item));
        if (accepted > 0) {
            amounts.put(item, count(item) + accepted);
            mutationVersion++;
        }
        return accepted;
    }

    int availableSpace(ItemId item) {
        Objects.requireNonNull(item, "item");
        int current = count(item);
        int stackLimit = item.stackLimit();
        int remainder = current % stackLimit;
        int partialSpace = remainder == 0 ? 0 : stackLimit - remainder;
        int freeSlots = Math.max(0, slotCapacity - usedSlots());
        long slotSpace = (long) partialSpace + (long) freeSlots * stackLimit;
        int totalSpace = MAX_AMOUNT_PER_ITEM - current;
        return (int) Math.max(0L, Math.min(slotSpace, totalSpace));
    }

    boolean canAccept(List<SaveGame.ItemStackData> items) {
        Inventory simulation = copy();
        if (items == null) return true;
        for (SaveGame.ItemStackData saved : items) {
            if (saved == null || saved.amount() <= 0) continue;
            ItemId item = ItemId.parse(saved.itemId()).orElse(null);
            if (item == null) continue;
            if (simulation.add(item, saved.amount()) != saved.amount()) return false;
        }
        return true;
    }

    boolean remove(ItemId item, int amount) {
        Objects.requireNonNull(item, "item");
        if (amount <= 0) return true;
        int current = count(item);
        if (current < amount) return false;
        amounts.put(item, current - amount);
        mutationVersion++;
        return true;
    }

    boolean transferTo(Inventory destination, ItemId item, int amount) {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(item, "item");
        if (amount <= 0) return true;
        if (!has(item, amount) || destination.availableSpace(item) < amount) return false;
        remove(item, amount);
        if (destination.add(item, amount) == amount) return true;
        add(item, amount);
        return false;
    }

    boolean transferAllTo(Inventory destination) {
        Objects.requireNonNull(destination, "destination");
        List<SaveGame.ItemStackData> contents = toSaveData();
        if (!destination.canAccept(contents)) return false;
        for (SaveGame.ItemStackData saved : contents) {
            ItemId item = ItemId.parse(saved.itemId()).orElse(null);
            if (item == null || saved.amount() <= 0) continue;
            transferTo(destination, item, saved.amount());
        }
        return true;
    }

    boolean has(ItemId item, int amount) {
        return amount <= 0 || count(item) >= amount;
    }

    void set(ItemId item, int amount) {
        Objects.requireNonNull(item, "item");
        int normalized = Math.max(0, Math.min(MAX_AMOUNT_PER_ITEM, amount));
        if (count(item) == normalized) return;
        amounts.put(item, normalized);
        mutationVersion++;
    }

    void clear() {
        boolean changed = false;
        for (ItemId item : ItemId.values()) {
            if (count(item) != 0) changed = true;
            amounts.put(item, 0);
        }
        if (changed) mutationVersion++;
    }

    boolean isEmpty() {
        for (ItemId item : ItemId.values()) if (count(item) > 0) return false;
        return true;
    }

    int slotCapacity() {
        return slotCapacity;
    }

    int usedSlots() {
        int result = 0;
        for (ItemId item : ItemId.values()) {
            int amount = count(item);
            if (amount > 0) result += (amount + item.stackLimit() - 1) / item.stackLimit();
        }
        return result;
    }

    boolean isOverCapacity() {
        return usedSlots() > slotCapacity;
    }

    long mutationVersion() {
        return mutationVersion;
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
