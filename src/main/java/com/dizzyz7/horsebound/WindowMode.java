// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum WindowMode {
    WINDOWED("Windowed"),
    FULLSCREEN("Fullscreen");

    private final String displayName;

    WindowMode(String displayName) {
        this.displayName = displayName;
    }

    String displayName() {
        return displayName;
    }

    WindowMode toggled() {
        return this == WINDOWED ? FULLSCREEN : WINDOWED;
    }
}
