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
    void profileRoundTripsWithBindingsIncludingInventory() {
        InputProfileRepository repository = new InputProfileRepository(tempDir.resolve("input.properties"));
        InputProfile expected = InputProfile.defaults()
            .withInvertCameraY(true)
            .withMoveDeadZone(0.28f)
            .withLookDeadZone(0.22f)
            .withSprintMode(SprintMode.TOGGLE)
            .withRumbleStrength(0.80f)
            .withBinding(BindableAction.INTERACT, Input.Keys.Q)
            .withBinding(BindableAction.INVENTORY, Input.Keys.TAB);

        repository.save(expected);

        assertEquals(expected, repository.load());
    }

    @Test
    void oldProfileWithoutInventoryBindingReceivesDefault() throws Exception {
        Path path = tempDir.resolve("input.properties");
        Files.writeString(path, "key.interact=" + Input.Keys.Q + "\n");

        InputProfile profile = new InputProfileRepository(path).load();

        assertEquals(Input.Keys.Q, profile.interactKey());
        assertEquals(BindableAction.INVENTORY.defaultKey(), profile.inventoryKey());
    }

    @Test
    void oldProfileUsingInventoryDefaultKeyKeepsItsExistingAction() throws Exception {
        Path path = tempDir.resolve("input.properties");
        Files.writeString(path, "key.interact=" + Input.Keys.I + "\n");

        InputProfile profile = new InputProfileRepository(path).load();

        assertEquals(Input.Keys.I, profile.interactKey());
        assertNotEquals(Input.Keys.I, profile.inventoryKey());
        for (BindableAction first : BindableAction.values()) {
            for (BindableAction second : BindableAction.values()) {
                if (first != second) assertNotEquals(profile.keyFor(first), profile.keyFor(second));
            }
        }
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
