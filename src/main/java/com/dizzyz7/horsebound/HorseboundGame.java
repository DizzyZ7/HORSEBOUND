// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import java.util.List;
import java.util.Objects;

public final class HorseboundGame extends Game {
    private final SettingsRepository settingsRepository;
    private final InputProfileRepository inputProfileRepository;
    private final FrameMetrics frameMetrics = new FrameMetrics();
    private GameSettings settings;
    private InputProfile inputProfile;
    private SaveService saveService;
    private PerformanceOverlay performanceOverlay;
    private PromptOverlay promptOverlay;
    private RanchSessionScreen suspendedWorld;

    public HorseboundGame() {
        this(
            new SettingsRepository(),
            GameSettings.defaults(),
            new InputProfileRepository(),
            InputProfile.defaults()
        );
    }

    HorseboundGame(SettingsRepository settingsRepository, GameSettings initialSettings) {
        this(settingsRepository, initialSettings, new InputProfileRepository(), InputProfile.defaults());
    }

    HorseboundGame(
        SettingsRepository settingsRepository,
        GameSettings initialSettings,
        InputProfileRepository inputProfileRepository,
        InputProfile initialInputProfile
    ) {
        this.settingsRepository = Objects.requireNonNull(settingsRepository, "settingsRepository");
        this.settings = initialSettings == null ? GameSettings.defaults() : initialSettings;
        this.inputProfileRepository = Objects.requireNonNull(inputProfileRepository, "inputProfileRepository");
        this.inputProfile = initialInputProfile == null ? InputProfile.defaults() : initialInputProfile;
    }

    @Override
    public void create() {
        saveService = new SaveService();
        performanceOverlay = new PerformanceOverlay();
        promptOverlay = new PromptOverlay();
        InputActivityTracker.reset();
        PauseRequestBus.reset();
        HomesteadActionBus.reset();
        HomesteadInputContext.reset();
        InputProfileContext.set(inputProfile);
        Gdx.graphics.setVSync(settings.vsync());
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        frameMetrics.record(Gdx.graphics.getDeltaTime());
        super.render();
        if (PauseRequestBus.consume() && getScreen() instanceof RanchSessionScreen world) {
            showPause(world);
            return;
        }
        if (promptOverlay != null) promptOverlay.render(getScreen(), settings);
        if (performanceOverlay != null) performanceOverlay.render(frameMetrics, settings);
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

    InputProfile inputProfile() {
        return inputProfile;
    }

    FrameMetrics frameMetrics() {
        return frameMetrics;
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

    void updateInputProfile(InputProfile next) {
        inputProfile = next == null ? InputProfile.defaults() : next;
        InputProfileContext.set(inputProfile);
        try {
            inputProfileRepository.save(inputProfile);
        } catch (InputProfileRepository.InputProfileException ex) {
            Gdx.app.error("HORSEBOUND", "Input profile save failed", ex);
        }
    }

    public void startNewWorld(String slotId) {
        suspendedWorld = null;
        SaveGame save = saveService.createNewWorld(slotId);
        switchTo(new HomesteadRanchScreen(this, saveService, save));
    }

    public void loadWorld(String slotId) {
        suspendedWorld = null;
        SaveGame save = saveService.loadWorld(slotId);
        switchTo(new HomesteadRanchScreen(this, saveService, save));
    }

    public void continueWorld() {
        suspendedWorld = null;
        SaveGame save = saveService.loadMostRecent();
        switchTo(new HomesteadRanchScreen(this, saveService, save));
    }

    public void showNewGameSlots() {
        switchTo(new SaveSlotsScreen(this, SaveSlotsScreen.Mode.NEW_GAME));
    }

    public void showLoadGameSlots() {
        switchTo(new SaveSlotsScreen(this, SaveSlotsScreen.Mode.LOAD_GAME));
    }

    public void showSettings() {
        switchTo(new SettingsHubScreen(this));
    }

    void showDisplaySettings() {
        switchTo(new SettingsScreen(this));
    }

    void showInputSettings(RanchSessionScreen pausedWorld) {
        if (pausedWorld != null) suspendedWorld = pausedWorld;
        switchTo(new InputSettingsScreen(this, pausedWorld));
    }

    void showKeyBindings(RanchSessionScreen pausedWorld) {
        if (pausedWorld != null) suspendedWorld = pausedWorld;
        switchTo(new KeyBindingsScreen(this, pausedWorld));
    }

    void showPause(RanchSessionScreen world) {
        if (world == null) return;
        suspendedWorld = world;
        Screen current = getScreen();
        PauseScreen pauseScreen = new PauseScreen(this, world);
        if (current == world) setScreen(pauseScreen); else switchTo(pauseScreen);
    }

    void resumePausedWorld(RanchSessionScreen world) {
        if (world == null) {
            returnToMenu();
            return;
        }
        Screen previous = getScreen();
        setScreen(world);
        if (previous != null && previous != world) previous.dispose();
        suspendedWorld = null;
        PauseRequestBus.reset();
        HomesteadActionBus.reset();
    }

    void leavePausedWorldToMenu(RanchSessionScreen world) {
        if (world != null) world.dispose();
        suspendedWorld = null;
        switchTo(new MenuScreen(this));
    }

    public void returnToMenu() {
        if (getScreen() instanceof SettingsScreen) switchTo(new SettingsHubScreen(this));
        else switchTo(new MenuScreen(this));
    }

    private void switchTo(Screen next) {
        Screen previous = getScreen();
        setScreen(Objects.requireNonNull(next, "next"));
        if (previous != null && previous != suspendedWorld) previous.dispose();
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        if (suspendedWorld != null && suspendedWorld != current) suspendedWorld.dispose();
        if (current != null) current.dispose();
        if (promptOverlay != null) promptOverlay.dispose();
        if (performanceOverlay != null) performanceOverlay.dispose();
        InputProfileContext.reset();
        PauseRequestBus.reset();
        HomesteadActionBus.reset();
        HomesteadInputContext.reset();
    }
}
