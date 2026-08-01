// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Input;

/** Keyboard actions that may be rebound without changing gameplay semantics. */
enum BindableAction {
    MOVE_FORWARD("Move forward", Input.Keys.W),
    MOVE_BACKWARD("Move backward", Input.Keys.S),
    MOVE_LEFT("Move left", Input.Keys.A),
    MOVE_RIGHT("Move right", Input.Keys.D),
    JUMP("Jump", Input.Keys.SPACE),
    INTERACT("Interact", Input.Keys.E),
    MOUNT("Mount / dismount", Input.Keys.F),
    BUILD("Build / edit", Input.Keys.B),
    INVENTORY("Inventory", Input.Keys.I),
    SPRINT("Sprint / gallop", Input.Keys.SHIFT_LEFT),
    SAVE("Manual save", Input.Keys.F5),
    PAUSE("Pause", Input.Keys.ESCAPE);

    private final String displayName;
    private final int defaultKey;

    BindableAction(String displayName, int defaultKey) {
        this.displayName = displayName;
        this.defaultKey = defaultKey;
    }

    String displayName() {
        return displayName;
    }

    int defaultKey() {
        return defaultKey;
    }
}
