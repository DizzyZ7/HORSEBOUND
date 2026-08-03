// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/** Device-local localization service with deterministic English fallback. */
final class I18n {
    private static final Map<String, String> ENGLISH = load("i18n/messages_en.properties");
    private static final Map<String, String> RUSSIAN = load("i18n/messages_ru.properties");
    private static volatile Language language = Language.ENGLISH;

    private I18n() {
    }

    static void setLanguage(Language next) {
        language = next == null ? Language.ENGLISH : next;
    }

    static Language language() {
        return language;
    }

    static String text(String key, Object... arguments) {
        Objects.requireNonNull(key, "key");
        String template = selected().get(key);
        if (template == null) template = ENGLISH.get(key);
        if (template == null) return "!" + key + "!";
        if (arguments == null || arguments.length == 0) return template;
        return new MessageFormat(template, language.locale()).format(arguments);
    }

    static String upper(String key, Object... arguments) {
        return text(key, arguments).toUpperCase(language.locale());
    }

    static Set<String> keys(Language target) {
        return target == Language.RUSSIAN ? RUSSIAN.keySet() : ENGLISH.keySet();
    }

    static String raw(Language target, String key) {
        Map<String, String> catalog = target == Language.RUSSIAN ? RUSSIAN : ENGLISH;
        return catalog.get(key);
    }

    private static Map<String, String> selected() {
        return language == Language.RUSSIAN ? RUSSIAN : ENGLISH;
    }

    private static Map<String, String> load(String resource) {
        Properties properties = new Properties();
        ClassLoader loader = I18n.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing localization catalog: " + resource);
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load localization catalog: " + resource, ex);
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) result.put(key, properties.getProperty(key));
        return Collections.unmodifiableMap(result);
    }
}
