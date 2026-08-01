// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Input;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InputProfileRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void missingProfileReturnsDefaults() {
        InputProfileRepository repository = new InputProfileRepository(tempDir.resolve("input.properties"));
        assertEquals(InputProfile.defaults(), repository.load());
    }

    @Test
    void profileRoundTripsWithBindings() {
        InputProfileRepository repository = new InputProfileRepository(tempDir.resolve("input.properties"));
        InputProfile expected = InputProfile.defaults()
            .withInvertCameraY(true)
            .withMoveDeadZone(0.28f)
            .withLookDeadZone(0.22f)
            .withSprintMode(SprintMode.TOGGLE)
            .withRumbleStrength(0.80f)
            .withBinding(BindableAction.INTERACT, Input.Keys.Q);

        repository.save(expected);

        assertEquals(expected, repository.load());
    }

    @Test
    void malformedProfileFallsBackPerField() throws Exception {
        Path path = tempDir.resolve("input.properties");
        Files.writeString(
            path,
            "invertCameraY=maybe\n"
                + "moveDeadZone=nope\n"
                + "lookDeadZone=8.0\n"
                + "sprintMode=warp\n"
                + "rumbleEnabled=false\n"
                + "rumbleStrength=NaN\n"
                + "key.jump=-9\n"
        );
        InputProfile profile = new InputProfileRepository(path).load();

        assertEquals(false, profile.invertCameraY());
        assertEquals(InputProfile.defaults().moveDeadZone(), profile.moveDeadZone());
        assertEquals(InputProfile.MAX_DEAD_ZONE, profile.lookDeadZone());
        assertEquals(SprintMode.HOLD, profile.sprintMode());
        assertEquals(false, profile.rumbleEnabled());
        assertEquals(InputProfile.defaults().rumbleStrength(), profile.rumbleStrength());
        assertEquals(BindableAction.JUMP.defaultKey(), profile.jumpKey());
    }

    @Test
    void duplicateKeysFromEditedFileAreNormalized() throws Exception {
        Path path = tempDir.resolve("input.properties");
        Files.writeString(
            path,
            "key.jump=" + Input.Keys.E + "\n"
                + "key.interact=" + Input.Keys.E + "\n"
        );

        InputProfile profile = new InputProfileRepository(path).load();

        assertNotEquals(profile.jumpKey(), profile.interactKey());
        for (BindableAction first : BindableAction.values()) {
            for (BindableAction second : BindableAction.values()) {
                if (first != second) assertNotEquals(profile.keyFor(first), profile.keyFor(second));
            }
        }
    }
}
