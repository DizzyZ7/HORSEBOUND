// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Optional;

final class SaveService {
    static final String DEFAULT_SLOT = "slot-1";

    private final SaveRepository repository;

    SaveService() {
        this(new SaveRepository());
    }

    SaveService(SaveRepository repository) {
        this.repository = repository;
    }

    boolean hasContinue() {
        return repository.exists(DEFAULT_SLOT);
    }

    SaveGame createNewWorld() {
        SaveGame saveGame = SaveGame.fresh(WorldSeed.random());
        repository.save(DEFAULT_SLOT, saveGame);
        return saveGame;
    }

    SaveGame loadContinue() {
        Optional<SaveGame> saveGame = repository.load(DEFAULT_SLOT);
        return saveGame.orElseGet(this::createNewWorld);
    }

    void save(SaveGame saveGame) {
        repository.save(DEFAULT_SLOT, saveGame);
    }

    String saveLocation() {
        return repository.root().resolve("saves").resolve(DEFAULT_SLOT).toString();
    }
}
