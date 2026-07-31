// HORSEBOUND — Created by Dimash Dzhanibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public final class HorseboundGame extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }

    public void startWorld() {
        switchTo(new WorldScreen(this));
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
