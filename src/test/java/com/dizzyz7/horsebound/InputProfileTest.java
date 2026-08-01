// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Input;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InputProfileTest {
    @Test
    void invalidValuesClampToSafeRanges() {
        InputProfile defaults = InputProfile.defaults();
        InputProfile profile = new InputProfile(
            true,
            -10f,
            Float.NaN,
            null,
            true,
            9f,
            -1, 999, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );

        assertEquals(InputProfile.MIN_DEAD_ZONE, profile.moveDeadZone());
        assertEquals(defaults.lookDeadZone(), profile.lookDeadZone());
        assertEquals(SprintMode.HOLD, profile.sprintMode());
        assertEquals(InputProfile.MAX_RUMBLE_STRENGTH, profile.rumbleStrength());
        assertEquals(BindableAction.MOVE_FORWARD.defaultKey(), profile.moveForwardKey());
        assertEquals(BindableAction.MOVE_BACKWARD.defaultKey(), profile.moveBackwardKey());
    }

    @Test
    void rebindingSwapsExistingKeyInsteadOfCreatingConflict() {
        InputProfile defaults = InputProfile.defaults();
        InputProfile rebound = defaults.withBinding(BindableAction.JUMP, Input.Keys.E);

        assertEquals(Input.Keys.E, rebound.jumpKey());
        assertEquals(Input.Keys.SPACE, rebound.interactKey());
        assertNotEquals(rebound.jumpKey(), rebound.interactKey());
    }

    @Test
    void allDefaultBindingsAreUnique() {
        InputProfile profile = InputProfile.defaults();
        for (BindableAction first : BindableAction.values()) {
            for (BindableAction second : BindableAction.values()) {
                if (first == second) continue;
                assertNotEquals(profile.keyFor(first), profile.keyFor(second));
            }
        }
    }
}
