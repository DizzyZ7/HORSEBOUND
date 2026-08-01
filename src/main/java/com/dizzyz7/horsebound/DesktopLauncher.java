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
        InputProfileRepository inputProfileRepository = new InputProfileRepository();
        InputProfile inputProfile = inputProfileRepository.load();
        InputProfileContext.set(inputProfile);
        GraphicsPreset preset = settings.graphicsPreset();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(buildInfo.displayLabel() + " | Created by DizZyZ7");
        DisplayController.configureStartup(config, settings);
        config.setResizable(true);
        config.setForegroundFPS(preset.foregroundFps());
        config.setIdleFPS(30);
        config.setBackBufferConfig(8, 8, 8, 8, 24, 8, preset.msaaSamples());
        new Lwjgl3Application(
            new HorseboundGame(settingsRepository, settings, inputProfileRepository, inputProfile),
            config
        );
    }
}
