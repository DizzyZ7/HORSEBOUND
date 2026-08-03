// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum WindowMode {
    WINDOWED("window.windowed"),
    FULLSCREEN("window.fullscreen");

    private final String displayKey;
    WindowMode(String displayKey) { this.displayKey = displayKey; }
    String displayName() { return I18n.text(displayKey); }
    WindowMode toggled() { return this == WINDOWED ? FULLSCREEN : WINDOWED; }
}
