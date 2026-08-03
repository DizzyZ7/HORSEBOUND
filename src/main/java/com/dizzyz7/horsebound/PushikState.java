// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum PushikState {
    FOLLOW,
    SIT,
    EXPLORE,
    SLEEP,
    GREET;

    String displayName() {
        return I18n.text("pushik." + name().toLowerCase(java.util.Locale.ROOT));
    }

    static PushikState parseOrDefault(String value) {
        if (value == null || value.isBlank()) return FOLLOW;
        try {
            return PushikState.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return FOLLOW;
        }
    }
}
