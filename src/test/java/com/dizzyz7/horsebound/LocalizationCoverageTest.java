// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationCoverageTest {
    private static final Pattern KEY_USE = Pattern.compile("I18n\\.(?:text|upper)\\(\\s*\"([^\"]+)\"");
    private static final List<String> PLAYER_FACING = List.of(
        "MenuScreen.java",
        "SettingsHubScreen.java",
        "SettingsScreen.java",
        "InputSettingsScreen.java",
        "KeyBindingsScreen.java",
        "PauseScreen.java",
        "SaveSlotsScreen.java",
        "InventoryOverlay.java",
        "PromptOverlay.java",
        "InputPromptCatalog.java",
        "GameplayHudCopy.java",
        "LivingRanchScreen.java",
        "HomesteadRanchScreen.java",
        "PerformanceOverlay.java"
    );

    @Test
    void everyLiteralLocalizationKeyExistsInEnglish() throws IOException {
        Path root = Path.of("src/main/java/com/dizzyz7/horsebound");
        try (var files = Files.list(root)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                Matcher matcher = KEY_USE.matcher(Files.readString(file));
                while (matcher.find()) {
                    String key = matcher.group(1);
                    if (key.endsWith(".")) continue; // Dynamic enum prefix validated by catalog parity tests.
                    assertTrue(I18n.keys(Language.ENGLISH).contains(key), file.getFileName() + ": " + key);
                }
            }
        }
    }

    @Test
    void playerFacingScreensDoNotDrawRawSentences() throws IOException {
        Path root = Path.of("src/main/java/com/dizzyz7/horsebound");
        for (String name : PLAYER_FACING) {
            String source = Files.readString(root.resolve(name));
            assertFalse(source.matches("(?s).*font\\.draw\\([^;]*,\\s*\"(?!HORSEBOUND)[A-Za-zА-Яа-я].*"), name);
            assertFalse(source.matches("(?s).*setStatus\\(\\s*\"[A-Za-zА-Яа-я].*"), name);
        }
    }

    @Test
    void allUiFontsUseTheUnicodeFactory() throws IOException {
        Path root = Path.of("src/main/java/com/dizzyz7/horsebound");
        for (String name : PLAYER_FACING) {
            String source = Files.readString(root.resolve(name));
            assertFalse(source.contains("new BitmapFont()"), name);
        }
    }
}
