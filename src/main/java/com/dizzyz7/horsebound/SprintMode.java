// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum SprintMode {
    HOLD("Hold"),
    TOGGLE("Toggle");

    private final String displayName;

    SprintMode(String displayName) {
        this.displayName = displayName;
    }

    String displayName() {
        return displayName;
    }

    SprintMode toggled() {
        return this == HOLD ? TOGGLE : HOLD;
    }
}
