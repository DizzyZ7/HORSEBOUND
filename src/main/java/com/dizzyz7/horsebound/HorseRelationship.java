// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/**
 * Persistent relationship state between one horse and the player.
 * Values are always normalized to 0..100.
 */
final class HorseRelationship {
    private float trust;
    private float bond;
    private float fear;

    HorseRelationship(float trust, float bond, float fear) {
        this.trust = clamp(trust);
        this.bond = clamp(bond);
        this.fear = clamp(fear);
    }

    static HorseRelationship wild() {
        return new HorseRelationship(0f, 0f, 12f);
    }

    float trust() {
        return trust;
    }

    float bond() {
        return bond;
    }

    float fear() {
        return fear;
    }

    void feed(HorsePersonality personality) {
        trust = clamp(trust + 28f * personality.trustGainMultiplier());
        bond = clamp(bond + 6f * personality.bondGainMultiplier());
        fear = clamp(fear - 12f / personality.fearSensitivity());
    }

    void pet(HorsePersonality personality) {
        trust = clamp(trust + 4f * personality.trustGainMultiplier());
        bond = clamp(bond + 8f * personality.bondGainMultiplier());
        fear = clamp(fear - 8f / personality.fearSensitivity());
    }

    void observeThreat(float intensity, HorsePersonality personality) {
        if (intensity <= 0f || !Float.isFinite(intensity)) {
            return;
        }
        fear = clamp(fear + intensity * personality.fearSensitivity());
        if (intensity > 6f) {
            trust = clamp(trust - intensity * 0.08f * personality.fearSensitivity());
        }
    }

    void calm(float deltaSeconds) {
        if (deltaSeconds <= 0f || !Float.isFinite(deltaSeconds)) {
            return;
        }
        fear = clamp(fear - 2.2f * deltaSeconds);
    }

    boolean isReadyToTame() {
        return trust >= 100f && fear <= 45f;
    }

    private static float clamp(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(0f, Math.min(100f, value));
    }
}
