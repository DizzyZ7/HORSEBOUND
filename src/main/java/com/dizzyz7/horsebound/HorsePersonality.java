// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Locale;
import java.util.UUID;

/**
 * A persistent temperament that changes how a horse reacts to the player.
 * Pure Java domain data: rendering and input do not belong here.
 */
enum HorsePersonality {
    CALM(1.10f, 0.65f, 1.00f),
    CURIOUS(1.25f, 0.90f, 1.08f),
    SHY(0.90f, 1.40f, 1.05f),
    BRAVE(1.00f, 0.50f, 1.00f),
    STUBBORN(0.78f, 0.80f, 0.90f),
    ENERGETIC(1.00f, 1.05f, 1.20f);

    private final float trustGainMultiplier;
    private final float fearSensitivity;
    private final float bondGainMultiplier;

    HorsePersonality(float trustGainMultiplier, float fearSensitivity, float bondGainMultiplier) {
        this.trustGainMultiplier = trustGainMultiplier;
        this.fearSensitivity = fearSensitivity;
        this.bondGainMultiplier = bondGainMultiplier;
    }

    float trustGainMultiplier() {
        return trustGainMultiplier;
    }

    float fearSensitivity() {
        return fearSensitivity;
    }

    float bondGainMultiplier() {
        return bondGainMultiplier;
    }

    String displayName() {
        String lower = name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    static HorsePersonality fromIdentity(UUID id) {
        HorsePersonality[] values = values();
        int mixed = Long.hashCode(id.getMostSignificantBits() ^ id.getLeastSignificantBits());
        return values[Math.floorMod(mixed, values.length)];
    }

    static HorsePersonality parseOrDefault(String raw, UUID id) {
        if (raw == null || raw.isBlank()) {
            return fromIdentity(id);
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fromIdentity(id);
        }
    }
}
