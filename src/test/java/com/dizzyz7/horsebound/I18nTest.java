// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nTest {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)}");

    @AfterEach
    void restoreEnglish() {
        I18n.setLanguage(Language.ENGLISH);
    }

    @Test
    void russianAndEnglishCatalogsAreCompleteAndNonBlank() {
        Set<String> english = I18n.keys(Language.ENGLISH);
        Set<String> russian = I18n.keys(Language.RUSSIAN);

        assertEquals(english, russian);
        assertTrue(english.size() >= 250);
        for (String key : english) {
            assertFalse(I18n.raw(Language.ENGLISH, key).isBlank(), key + " English value");
            assertFalse(I18n.raw(Language.RUSSIAN, key).isBlank(), key + " Russian value");
            assertEquals(
                placeholders(I18n.raw(Language.ENGLISH, key)),
                placeholders(I18n.raw(Language.RUSSIAN, key)),
                key + " placeholders"
            );
        }
    }

    @Test
    void languageSwitchAndFormattingAreImmediate() {
        I18n.setLanguage(Language.ENGLISH);
        assertEquals("Stored 3 Wood.", I18n.text("inventory.stored", 3, "Wood"));

        I18n.setLanguage(Language.RUSSIAN);
        assertEquals("Сложено: 3 × Дерево.", I18n.text("inventory.stored", 3, "Дерево"));
        assertEquals("НАСТРОЙКИ", I18n.upper("settings.title"));
    }

    @Test
    void unknownKeyFailsVisiblyInsteadOfRenderingBlankCopy() {
        assertEquals("!missing.test.key!", I18n.text("missing.test.key"));
    }

    private static Set<String> placeholders(String text) {
        java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(text);
        while (matcher.find()) result.add(matcher.group(1));
        return Set.copyOf(result);
    }
}
