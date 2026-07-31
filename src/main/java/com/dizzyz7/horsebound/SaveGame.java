// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

record SaveGame(
    int saveVersion,
    long worldSeed,
    long savedAtEpochMillis,
    float worldTime,
    PlayerData player,
    PushikData pushik,
    List<HorseData> horses,
    List<FenceData> fences,
    List<Integer> harvestedTreeIds
) {
    static final int CURRENT_VERSION = 2;

    SaveGame {
        player = Objects.requireNonNull(player, "player");
        pushik = Objects.requireNonNull(pushik, "pushik");
        horses = List.copyOf(Objects.requireNonNull(horses, "horses"));
        fences = List.copyOf(Objects.requireNonNull(fences, "fences"));
        harvestedTreeIds = List.copyOf(Objects.requireNonNull(harvestedTreeIds, "harvestedTreeIds"));
    }

    static SaveGame fresh(WorldSeed seed) {
        return new SaveGame(
            CURRENT_VERSION,
            seed.value(),
            System.currentTimeMillis(),
            0.29f,
            new PlayerData(0f, -18f, 0f, 4, 5),
            new PushikData(2f, -16f, 0f),
            List.of(),
            List.of(),
            List.of()
        );
    }

    record PlayerData(float x, float z, float facing, int wood, int apples) {
    }

    record PushikData(float x, float z, float heading) {
    }

    record HorseData(
        UUID id,
        String name,
        float x,
        float z,
        float heading,
        float trust,
        float stamina,
        boolean tamed,
        HorsePersonality personality,
        float bond,
        float fear
    ) {
        HorseData {
            id = Objects.requireNonNull(id, "id");
            name = Objects.requireNonNull(name, "name");
            personality = Objects.requireNonNull(personality, "personality");
            trust = clampPercent(trust);
            stamina = clampPercent(stamina);
            bond = clampPercent(bond);
            fear = clampPercent(fear);
        }

        /**
         * Compatibility constructor used by v1 migration and older tests/call sites.
         */
        HorseData(
            UUID id,
            String name,
            float x,
            float z,
            float heading,
            float trust,
            float stamina,
            boolean tamed
        ) {
            this(
                id,
                name,
                x,
                z,
                heading,
                trust,
                stamina,
                tamed,
                HorsePersonality.fromIdentity(id),
                tamed ? Math.max(20f, trust * 0.55f) : trust * 0.25f,
                tamed ? 5f : 12f
            );
        }
    }

    record FenceData(float x, float z, float heading) {
    }

    private static float clampPercent(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return Math.max(0f, Math.min(100f, value));
    }
}
