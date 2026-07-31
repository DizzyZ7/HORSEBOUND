// HORSEBOUND — Created by Dimash Dzhanibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(AuthorInfo.GAME_NAME + " | Created by DizZyZ7");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(144);
        config.setBackBufferConfig(8, 8, 8, 8, 24, 8, 4);
        new Lwjgl3Application(new HorseboundGame(), config);
    }
}
