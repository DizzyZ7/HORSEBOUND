// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import java.util.List;

public final class HorseboundGame extends Game {
    private final SettingsRepository settingsRepository;
    private GameSettings settings;
    private SaveService saveService;

    public HorseboundGame() {
        this(new SettingsRepository(), GameSettings.defaults());
    }

    HorseboundGame(SettingsRepository settingsRepository, GameSettings initialSettings) {
        this.settingsRepository = settingsRepository;
        this.settings = initialSettings;
    }

    @Override
    public void create() {
        saveService = new SaveService();
        Gdx.graphics.setVSync(settings.vsync());
        setScreen(new MenuScreen(this));
    }

    boolean hasContinue() {
        return saveService.hasContinue();
    }

    List<SaveSlotInfo> saveSlots() {
        return saveService.listSlots();
    }

    GameSettings settings() {
        return settings;
    }

    void updateSettings(GameSettings next) {
        GameSettings applied = DisplayController.applyRuntime(settings, next);
        settings = applied;
        try {
            settingsRepository.save(applied);
        } catch (SettingsRepository.SettingsException ex) {
            Gdx.app.error("HORSEBOUND", "Settings save failed", ex);
        }
    }

    public void startNewWorld(String slotId) {
        switchTo(new LivingRanchScreen(this, saveService, saveService.createNewWorld(slotId)));
    }

    public void loadWorld(String slotId) {
        switchTo(new LivingRanchScreen(this, saveService, saveService.loadWorld(slotId)));
    }

    public void continueWorld() {
        switchTo(new LivingRanchScreen(this, saveService, saveService.loadMostRecent()));
    }

    public void showNewGameSlots() {
        switchTo(new SaveSlotsScreen(this, SaveSlotsScreen.Mode.NEW_GAME));
    }

    public void showLoadGameSlots() {
        switchTo(new SaveSlotsScreen(this, SaveSlotsScreen.Mode.LOAD_GAME));
    }

    public void showSettings() {
        switchTo(new SettingsScreen(this));
    }

    public void returnToMenu() {
        switchTo(new MenuScreen(this));
    }

    private void switchTo(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) previous.dispose();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
    }
}
