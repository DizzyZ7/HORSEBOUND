// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Locale;

enum Language {
    ENGLISH(Locale.ENGLISH, "English"),
    RUSSIAN(Locale.forLanguageTag("ru"), "Русский");

    private final Locale locale;
    private final String selfName;

    Language(Locale locale, String selfName) {
        this.locale = locale;
        this.selfName = selfName;
    }

    Locale locale() {
        return locale;
    }

    String selfName() {
        return selfName;
    }

    Language toggled() {
        return this == ENGLISH ? RUSSIAN : ENGLISH;
    }

    static Language systemDefault() {
        return "ru".equalsIgnoreCase(Locale.getDefault().getLanguage()) ? RUSSIAN : ENGLISH;
    }
}
