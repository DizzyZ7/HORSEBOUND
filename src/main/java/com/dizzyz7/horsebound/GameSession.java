// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class GameSession {
    private final long worldSeed;
    private final Inventory inventory;
    private final PushikMind pushikMind;
    private final FixedStepClock simulationClock;
    private float worldTime;
    private long simulationTicks;

    GameSession(SaveGame initialState) {
        this(initialState, new FixedStepClock());
    }

    GameSession(SaveGame initialState, FixedStepClock simulationClock) {
        this.worldSeed = initialState.worldSeed();
        this.worldTime = normalizeTime(initialState.worldTime());
        this.inventory = Inventory.restore(
            initialState.player().inventoryItems(),
            initialState.player().wood(),
            initialState.player().apples()
        );
        this.pushikMind = new PushikMind(initialState.pushik().state(), initialState.pushik().affection());
        this.simulationClock = simulationClock;
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

    long simulationTicks() {
        return simulationTicks;
    }

    float simulationInterpolationAlpha() {
        return simulationClock.interpolationAlpha();
    }

    int advanceWorldTime(float frameDeltaSeconds) {
        return simulationClock.advance(frameDeltaSeconds, this::updateFixed);
    }

    private void updateFixed(float fixedDeltaSeconds) {
        simulationTicks++;
        worldTime = normalizeTime(worldTime + fixedDeltaSeconds / 1200f);
    }

    private static float normalizeTime(float value) {
        if (!Float.isFinite(value)) return 0.29f;
        float normalized = value % 1f;
        return normalized < 0f ? normalized + 1f : normalized;
    }
}
