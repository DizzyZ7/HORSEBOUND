// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class Hotbar {
    static final int SLOT_COUNT = 8;

    private final List<ItemId> slots;
    private int selectedIndex;

    Hotbar(List<ItemId> slots, int selectedIndex) {
        List<ItemId> normalized = new ArrayList<>(SLOT_COUNT);
        if (slots != null) {
            for (ItemId item : slots) {
                if (normalized.size() >= SLOT_COUNT) break;
                normalized.add(item);
            }
        }
        while (normalized.size() < SLOT_COUNT) normalized.add(null);
        this.slots = normalized;
        this.selectedIndex = Math.floorMod(selectedIndex, SLOT_COUNT);
    }

    static Hotbar defaults() {
        return new Hotbar(
            List.of(
                ItemId.WOOD,
                ItemId.HAY,
                ItemId.WATER_BUCKET,
                ItemId.APPLE,
                ItemId.CARROT,
                ItemId.STONE
            ),
            0
        );
    }

    static Hotbar restore(SaveGame.HotbarData data) {
        if (data == null) return defaults();
        List<ItemId> items = new ArrayList<>();
        for (String raw : data.itemIds()) items.add(ItemId.parse(raw).orElse(null));
        return new Hotbar(items, data.selectedIndex());
    }

    int selectedIndex() {
        return selectedIndex;
    }

    ItemId selectedItem() {
        return slots.get(selectedIndex);
    }

    ItemId itemAt(int index) {
        if (index < 0 || index >= SLOT_COUNT) throw new IndexOutOfBoundsException(index);
        return slots.get(index);
    }

    void select(int index) {
        selectedIndex = Math.floorMod(index, SLOT_COUNT);
    }

    void cycle(int delta) {
        select(selectedIndex + delta);
    }

    void assign(int index, ItemId item) {
        if (index < 0 || index >= SLOT_COUNT) throw new IndexOutOfBoundsException(index);
        slots.set(index, item);
    }

    List<ItemId> slots() {
        return Collections.unmodifiableList(new ArrayList<>(slots));
    }

    SaveGame.HotbarData toSaveData() {
        List<String> ids = slots.stream().map(item -> item == null ? "" : item.name()).toList();
        return new SaveGame.HotbarData(selectedIndex, ids);
    }
}
