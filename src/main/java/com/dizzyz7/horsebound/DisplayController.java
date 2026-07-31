// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

final class DisplayController {
    private DisplayController() {
    }

    static void configureStartup(Lwjgl3ApplicationConfiguration config, GameSettings settings) {
        if (settings.windowMode() == WindowMode.FULLSCREEN) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(settings.windowWidth(), settings.windowHeight());
        }
        config.useVsync(settings.vsync());
    }

    static GameSettings applyRuntime(GameSettings previous, GameSettings requested) {
        Gdx.graphics.setVSync(requested.vsync());
        boolean displayChanged = previous.windowMode() != requested.windowMode()
            || previous.windowWidth() != requested.windowWidth()
            || previous.windowHeight() != requested.windowHeight();
        if (!displayChanged) return requested;

        if (requested.windowMode() == WindowMode.FULLSCREEN) {
            if (!Gdx.graphics.supportsDisplayModeChange()) {
                return requested.withWindowMode(WindowMode.WINDOWED);
            }
            Graphics.Monitor monitor = Gdx.graphics.getMonitor();
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode(monitor);
            if (!Gdx.graphics.setFullscreenMode(mode)) {
                Gdx.graphics.setWindowedMode(requested.windowWidth(), requested.windowHeight());
                return requested.withWindowMode(WindowMode.WINDOWED);
            }
            return requested;
        }

        if (!Gdx.graphics.setWindowedMode(requested.windowWidth(), requested.windowHeight())) {
            return previous;
        }
        return requested;
    }
}
