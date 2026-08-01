// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Screen;

/** A live ranch session that can be paused, resumed, saved and safely edited without reconstruction. */
interface RanchSessionScreen extends Screen {
    void saveSession();

    default boolean hasUndoableRanchEdit() {
        return false;
    }

    default String undoLastRanchEdit() {
        return "Nothing to undo in this ranch session.";
    }
}
