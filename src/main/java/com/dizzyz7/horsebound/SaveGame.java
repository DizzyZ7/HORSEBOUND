// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    List<StructureData> structures,
    HotbarData hotbar,
    List<Integer> harvestedTreeIds
) {
    static final int CURRENT_VERSION = 5;

    SaveGame {
        player = Objects.requireNonNull(player, "player");
        pushik = Objects.requireNonNull(pushik, "pushik");
        horses = List.copyOf(Objects.requireNonNull(horses, "horses"));
        fences = List.copyOf(Objects.requireNonNull(fences, "fences"));
        structures = List.copyOf(Objects.requireNonNull(structures, "structures"));
        hotbar = Objects.requireNonNullElseGet(hotbar, () -> Hotbar.defaults().toSaveData());
        harvestedTreeIds = List.copyOf(Objects.requireNonNull(harvestedTreeIds, "harvestedTreeIds"));
    }

    /** Compatibility constructor for the 0.4 gameplay screen and older tests/call sites. */
    SaveGame(
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
        this(
            saveVersion,
            worldSeed,
            savedAtEpochMillis,
            worldTime,
            player,
            pushik,
            horses,
            fences,
            structuresFromLegacyFences(worldSeed, fences),
            Hotbar.defaults().toSaveData(),
            harvestedTreeIds
        );
    }

    static SaveGame fresh(WorldSeed seed) {
        return new SaveGame(
            CURRENT_VERSION,
            seed.value(),
            System.currentTimeMillis(),
            0.29f,
            new PlayerData(
                0f,
                -18f,
                0f,
                4,
                5,
                List.of(
                    new ItemStackData(ItemId.WOOD.name(), 4),
                    new ItemStackData(ItemId.STONE.name(), 8),
                    new ItemStackData(ItemId.APPLE.name(), 5),
                    new ItemStackData(ItemId.HAY.name(), 6),
                    new ItemStackData(ItemId.WATER_BUCKET.name(), 2)
                )
            ),
            new PushikData(2f, -16f, 0f, 45f, PushikState.FOLLOW),
            List.of(),
            List.of(),
            List.of(),
            Hotbar.defaults().toSaveData(),
            List.of()
        );
    }

    record PlayerData(
        float x,
        float z,
        float facing,
        int wood,
        int apples,
        List<ItemStackData> inventoryItems
    ) {
        PlayerData {
            inventoryItems = List.copyOf(Objects.requireNonNull(inventoryItems, "inventoryItems"));
        }

        /** Compatibility constructor for v1/v2 saves and older call sites. */
        PlayerData(float x, float z, float facing, int wood, int apples) {
            this(
                x,
                z,
                facing,
                wood,
                apples,
                List.of(
                    new ItemStackData(ItemId.WOOD.name(), Math.max(0, wood)),
                    new ItemStackData(ItemId.APPLE.name(), Math.max(0, apples))
                )
            );
        }
    }

    record ItemStackData(String itemId, int amount) {
        ItemStackData {
            itemId = Objects.requireNonNull(itemId, "itemId");
            amount = Math.max(0, amount);
        }
    }

    record HotbarData(int selectedIndex, List<String> itemIds) {
        HotbarData {
            selectedIndex = Math.floorMod(selectedIndex, Hotbar.SLOT_COUNT);
            List<String> normalized = new ArrayList<>(Hotbar.SLOT_COUNT);
            if (itemIds != null) {
                for (String id : itemIds) {
                    if (normalized.size() >= Hotbar.SLOT_COUNT) break;
                    normalized.add(id == null ? "" : id);
                }
            }
            while (normalized.size() < Hotbar.SLOT_COUNT) normalized.add("");
            itemIds = List.copyOf(normalized);
        }
    }

    record PushikData(float x, float z, float heading, float affection, PushikState state) {
        PushikData {
            affection = clampPercent(affection);
            state = Objects.requireNonNull(state, "state");
        }

        /** Compatibility constructor for v1/v2 saves and older call sites. */
        PushikData(float x, float z, float heading) {
            this(x, z, heading, 45f, PushikState.FOLLOW);
        }
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
        float fear,
        float hunger,
        float thirst,
        float energy
    ) {
        HorseData {
            id = Objects.requireNonNull(id, "id");
            name = Objects.requireNonNull(name, "name");
            personality = Objects.requireNonNull(personality, "personality");
            trust = clampPercent(trust);
            stamina = clampPercent(stamina);
            bond = clampPercent(bond);
            fear = clampPercent(fear);
            hunger = clampPercent(hunger);
            thirst = clampPercent(thirst);
            energy = clampPercent(energy);
        }

        /** Compatibility constructor used by v2/v3/v4 saves and current gameplay actors. */
        HorseData(
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
            this(
                id,
                name,
                x,
                z,
                heading,
                trust,
                stamina,
                tamed,
                personality,
                bond,
                fear,
                HorseNeeds.healthy().hunger(),
                HorseNeeds.healthy().thirst(),
                HorseNeeds.healthy().energy()
            );
        }

        /** Compatibility constructor used by v1 migration and older tests/call sites. */
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

        HorseNeeds needs() {
            return new HorseNeeds(hunger, thirst, energy);
        }
    }

    record FenceData(float x, float z, float heading) {
    }

    record StructureData(
        UUID id,
        HomesteadStructureType type,
        float x,
        float z,
        float heading,
        int storedUnits,
        boolean open,
        List<ItemStackData> storedItems
    ) {
        StructureData {
            id = Objects.requireNonNull(id, "id");
            type = Objects.requireNonNull(type, "type");
            x = finiteOrZero(x);
            z = finiteOrZero(z);
            heading = normalizeHeading(heading);
            storedUnits = Math.max(0, Math.min(type.storageCapacity(), storedUnits));
            open = type.canToggleOpen() && open;
            storedItems = type.storesItems()
                ? List.copyOf(Objects.requireNonNullElse(storedItems, List.of()))
                : List.of();
        }

        /** Compatibility constructor for save v4 and older call sites. */
        StructureData(
            UUID id,
            HomesteadStructureType type,
            float x,
            float z,
            float heading,
            int storedUnits
        ) {
            this(id, type, x, z, heading, storedUnits, false, List.of());
        }
    }

    private static List<StructureData> structuresFromLegacyFences(long worldSeed, List<FenceData> fences) {
        if (fences == null || fences.isEmpty()) return List.of();
        List<StructureData> result = new ArrayList<>(fences.size());
        for (int i = 0; i < fences.size(); i++) {
            FenceData fence = fences.get(i);
            String identity = worldSeed + ":legacy-fence:" + i + ":" + fence.x() + ":" + fence.z() + ":" + fence.heading();
            result.add(new StructureData(
                UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8)),
                HomesteadStructureType.FENCE,
                fence.x(),
                fence.z(),
                fence.heading(),
                0
            ));
        }
        return List.copyOf(result);
    }

    private static float clampPercent(float value) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(0f, Math.min(100f, value));
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0f;
    }

    private static float normalizeHeading(float value) {
        if (!Float.isFinite(value)) return 0f;
        float normalized = value % 360f;
        return normalized < 0f ? normalized + 360f : normalized;
    }
}
