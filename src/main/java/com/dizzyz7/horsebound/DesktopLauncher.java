// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.util.Arrays;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        CrashReportService.installDefaultHandler();
        BuildInfo buildInfo = BuildInfo.current();

        if (Arrays.asList(args).contains("--version")) {
            System.out.println(buildInfo.diagnosticLabel());
            System.out.println("Created by " + AuthorInfo.CREATOR);
            return;
        }

        SettingsRepository settingsRepository = new SettingsRepository();
        GameSettings settings = settingsRepository.load();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(buildInfo.displayLabel() + " | Created by DizZyZ7");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.useVsync(settings.vsync());
        config.setForegroundFPS(144);
        config.setBackBufferConfig(8, 8, 8, 8, 24, 8, 4);
        new Lwjgl3Application(new HorseboundGame(settingsRepository, settings), config);
    }
}
