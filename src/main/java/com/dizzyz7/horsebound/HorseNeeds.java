// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

record HorseNeeds(float hunger, float thirst, float energy) {
    private static final float PASSIVE_HUNGER_PER_SECOND = 0.020f;
    private static final float PASSIVE_THIRST_PER_SECOND = 0.030f;
    private static final float PASSIVE_ENERGY_PER_SECOND = 0.010f;

    HorseNeeds {
        hunger = clamp(hunger);
        thirst = clamp(thirst);
        energy = clamp(energy);
    }

    static HorseNeeds healthy() {
        return new HorseNeeds(82f, 78f, 90f);
    }

    HorseNeeds tick(float seconds, boolean moving, boolean galloping) {
        if (!Float.isFinite(seconds) || seconds <= 0f) return this;
        float activity = galloping ? 4.0f : moving ? 1.8f : 1f;
        float nextHunger = hunger - PASSIVE_HUNGER_PER_SECOND * activity * seconds;
        float nextThirst = thirst - PASSIVE_THIRST_PER_SECOND * activity * seconds;
        float energyDrain = PASSIVE_ENERGY_PER_SECOND * activity * seconds;
        float nextEnergy = moving ? energy - energyDrain : energy + 0.065f * seconds;
        return new HorseNeeds(nextHunger, nextThirst, nextEnergy);
    }

    HorseNeeds feed(float nutrition) {
        return new HorseNeeds(hunger + positive(nutrition), thirst, energy);
    }

    HorseNeeds water(float hydration) {
        return new HorseNeeds(hunger, thirst + positive(hydration), energy);
    }

    HorseNeeds rest(float recovery) {
        return new HorseNeeds(hunger, thirst, energy + positive(recovery));
    }

    boolean needsFeed() {
        return hunger < 45f;
    }

    boolean needsWater() {
        return thirst < 45f;
    }

    boolean needsRest() {
        return energy < 35f;
    }

    float wellbeing() {
        return (hunger + thirst + energy) / 3f;
    }

    private static float positive(float value) {
        return Float.isFinite(value) ? Math.max(0f, value) : 0f;
    }

    private static float clamp(float value) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(0f, Math.min(100f, value));
    }
}
