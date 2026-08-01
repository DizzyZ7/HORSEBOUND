// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

final class SaveService {
    static final String DEFAULT_SLOT = "slot-1";
    static final List<String> SLOT_IDS = List.of("slot-1", "slot-2", "slot-3");
    private static final int STARTER_STONE = 8;

    private final SaveRepository repository;
    private String activeSlot = DEFAULT_SLOT;
    private Object transformerOwner;
    private UnaryOperator<SaveGame> saveTransformer = UnaryOperator.identity();

    SaveService() {
        this(new SaveRepository());
    }

    SaveService(SaveRepository repository) {
        this.repository = repository;
    }

    boolean hasContinue() {
        return mostRecentReadySlot().isPresent();
    }

    List<SaveSlotInfo> listSlots() {
        List<SaveSlotInfo> slots = new ArrayList<>(SLOT_IDS.size());
        for (int i = 0; i < SLOT_IDS.size(); i++) {
            String slotId = SLOT_IDS.get(i);
            String label = "Ranch " + (i + 1);
            if (!repository.exists(slotId)) {
                slots.add(new SaveSlotInfo(slotId, label, SaveSlotInfo.State.EMPTY, 0L, 0L, 0, 0, 0));
                continue;
            }

            try {
                Optional<SaveGame> loaded = repository.load(slotId);
                if (loaded.isEmpty()) {
                    slots.add(new SaveSlotInfo(slotId, label, SaveSlotInfo.State.EMPTY, 0L, 0L, 0, 0, 0));
                    continue;
                }
                SaveGame save = loaded.get();
                int tamed = 0;
                for (SaveGame.HorseData horse : save.horses()) if (horse.tamed()) tamed++;
                slots.add(new SaveSlotInfo(
                    slotId,
                    label,
                    SaveSlotInfo.State.READY,
                    save.savedAtEpochMillis(),
                    save.worldSeed(),
                    save.horses().size(),
                    tamed,
                    save.structures().size()
                ));
            } catch (SaveRepository.SaveException ex) {
                slots.add(new SaveSlotInfo(slotId, label, SaveSlotInfo.State.CORRUPT, 0L, 0L, 0, 0, 0));
            }
        }
        return List.copyOf(slots);
    }

    Optional<SaveSlotInfo> mostRecentReadySlot() {
        return listSlots().stream()
            .filter(SaveSlotInfo::canLoad)
            .max(Comparator.comparingLong(SaveSlotInfo::savedAtEpochMillis));
    }

    SaveGame createNewWorld(String slotId) {
        validateSlot(slotId);
        activeSlot = slotId;
        SaveGame saveGame = withStarterHomesteadMaterials(SaveGame.fresh(WorldSeed.random()));
        repository.save(activeSlot, saveGame);
        return saveGame;
    }

    SaveGame loadWorld(String slotId) {
        validateSlot(slotId);
        SaveGame saveGame = repository.load(slotId)
            .orElseThrow(() -> new SaveRepository.SaveException("Save slot is empty: " + slotId, null));
        activeSlot = slotId;
        return saveGame;
    }

    SaveGame loadMostRecent() {
        SaveSlotInfo slot = mostRecentReadySlot()
            .orElseThrow(() -> new SaveRepository.SaveException("No HORSEBOUND save is available to continue.", null));
        return loadWorld(slot.slotId());
    }

    void save(SaveGame saveGame) {
        SaveGame transformed = saveTransformer.apply(Objects.requireNonNull(saveGame, "saveGame"));
        repository.save(activeSlot, Objects.requireNonNull(transformed, "transformed saveGame"));
    }

    void setSaveTransformer(Object owner, UnaryOperator<SaveGame> transformer) {
        transformerOwner = Objects.requireNonNull(owner, "owner");
        saveTransformer = Objects.requireNonNull(transformer, "transformer");
    }

    void clearSaveTransformer(Object owner) {
        if (transformerOwner != owner) return;
        transformerOwner = null;
        saveTransformer = UnaryOperator.identity();
    }

    String activeSlot() {
        return activeSlot;
    }

    String saveLocation() {
        return repository.root().resolve("saves").resolve(activeSlot).toString();
    }

    private static SaveGame withStarterHomesteadMaterials(SaveGame source) {
        Inventory inventory = Inventory.restore(
            source.player().inventoryItems(),
            source.player().wood(),
            source.player().apples()
        );
        inventory.add(ItemId.STONE, STARTER_STONE);
        SaveGame.PlayerData player = new SaveGame.PlayerData(
            source.player().x(),
            source.player().z(),
            source.player().facing(),
            inventory.count(ItemId.WOOD),
            inventory.count(ItemId.APPLE),
            inventory.toSaveData()
        );
        return new SaveGame(
            SaveGame.CURRENT_VERSION,
            source.worldSeed(),
            source.savedAtEpochMillis(),
            source.worldTime(),
            player,
            source.pushik(),
            source.horses(),
            source.fences(),
            source.structures(),
            source.hotbar(),
            source.harvestedTreeIds()
        );
    }

    private static void validateSlot(String slotId) {
        if (!SLOT_IDS.contains(slotId)) {
            throw new IllegalArgumentException("Unsupported HORSEBOUND save slot: " + slotId);
        }
    }
}
