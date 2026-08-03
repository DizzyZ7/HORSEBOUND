// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum SprintMode {
    HOLD("sprint.hold"),
    TOGGLE("sprint.toggle");

    private final String displayKey;
    SprintMode(String displayKey) { this.displayKey = displayKey; }
    String displayName() { return I18n.text(displayKey); }
    SprintMode toggled() { return this == HOLD ? TOGGLE : HOLD; }
}
