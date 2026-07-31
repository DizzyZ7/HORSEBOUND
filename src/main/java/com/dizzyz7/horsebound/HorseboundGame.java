// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public final class HorseboundGame extends Game {
    private SaveService saveService;

    @Override
    public void create() {
        saveService = new SaveService();
        setScreen(new MenuScreen(this));
    }

    boolean hasContinue() {
        return saveService.hasContinue();
    }

    public void startNewWorld() {
        switchTo(new WorldScreen(this, saveService, saveService.createNewWorld()));
    }

    public void continueWorld() {
        switchTo(new WorldScreen(this, saveService, saveService.loadContinue()));
    }

    public void returnToMenu() {
        switchTo(new MenuScreen(this));
    }

    private void switchTo(Screen next) {
        Screen previous = getScreen();
        setScreen(next);
        if (previous != null) {
            previous.dispose();
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
