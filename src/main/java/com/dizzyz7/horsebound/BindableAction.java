// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Input;

/** Keyboard actions that may be rebound without changing gameplay semantics. */
enum BindableAction {
    MOVE_FORWARD("action.move_forward", Input.Keys.W),
    MOVE_BACKWARD("action.move_backward", Input.Keys.S),
    MOVE_LEFT("action.move_left", Input.Keys.A),
    MOVE_RIGHT("action.move_right", Input.Keys.D),
    JUMP("action.jump", Input.Keys.SPACE),
    INTERACT("action.interact", Input.Keys.E),
    MOUNT("action.mount", Input.Keys.F),
    BUILD("action.build", Input.Keys.B),
    INVENTORY("action.inventory", Input.Keys.I),
    SPRINT("action.sprint", Input.Keys.SHIFT_LEFT),
    SAVE("action.save", Input.Keys.F5),
    PAUSE("action.pause", Input.Keys.ESCAPE);

    private final String displayKey;
    private final int defaultKey;

    BindableAction(String displayKey, int defaultKey) {
        this.displayKey = displayKey;
        this.defaultKey = defaultKey;
    }

    String displayName() { return I18n.text(displayKey); }
    int defaultKey() { return defaultKey; }
}
