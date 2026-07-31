// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class GameSession {
    private final long worldSeed;
    private final Inventory inventory;
    private final PushikMind pushikMind = new PushikMind();
    private float worldTime;

    GameSession(SaveGame initialState) {
        this.worldSeed = initialState.worldSeed();
        this.worldTime = normalizeTime(initialState.worldTime());
        this.inventory = Inventory.starter(initialState.player().wood(), initialState.player().apples());
    }

    long worldSeed() {
        return worldSeed;
    }

    Inventory inventory() {
        return inventory;
    }

    PushikMind pushikMind() {
        return pushikMind;
    }

    float worldTime() {
        return worldTime;
    }

    void advanceWorldTime(float dt) {
        worldTime = normalizeTime(worldTime + Math.max(0f, dt) / 1200f);
    }

    private static float normalizeTime(float value) {
        if (!Float.isFinite(value)) return 0.29f;
        float normalized = value % 1f;
        return normalized < 0f ? normalized + 1f : normalized;
    }
}
