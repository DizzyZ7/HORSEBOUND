// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class SaveService {
    static final String DEFAULT_SLOT = "slot-1";
    static final List<String> SLOT_IDS = List.of("slot-1", "slot-2", "slot-3");

    private final SaveRepository repository;
    private String activeSlot = DEFAULT_SLOT;

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
                for (SaveGame.HorseData horse : save.horses()) {
                    if (horse.tamed()) {
                        tamed++;
                    }
                }
                slots.add(new SaveSlotInfo(
                    slotId,
                    label,
                    SaveSlotInfo.State.READY,
                    save.savedAtEpochMillis(),
                    save.worldSeed(),
                    save.horses().size(),
                    tamed,
                    save.fences().size()
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
        SaveGame saveGame = SaveGame.fresh(WorldSeed.random());
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
        repository.save(activeSlot, saveGame);
    }

    String activeSlot() {
        return activeSlot;
    }

    String saveLocation() {
        return repository.root().resolve("saves").resolve(activeSlot).toString();
    }

    private static void validateSlot(String slotId) {
        if (!SLOT_IDS.contains(slotId)) {
            throw new IllegalArgumentException("Unsupported HORSEBOUND save slot: " + slotId);
        }
    }
}
