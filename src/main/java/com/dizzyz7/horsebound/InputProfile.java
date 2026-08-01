// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Device-local input and accessibility settings, intentionally separate from ranch saves. */
record InputProfile(
    boolean invertCameraY,
    float moveDeadZone,
    float lookDeadZone,
    SprintMode sprintMode,
    boolean rumbleEnabled,
    float rumbleStrength,
    int moveForwardKey,
    int moveBackwardKey,
    int moveLeftKey,
    int moveRightKey,
    int jumpKey,
    int interactKey,
    int mountKey,
    int buildKey,
    int sprintKey,
    int saveKey,
    int pauseKey
) {
    static final float MIN_DEAD_ZONE = 0.05f;
    static final float MAX_DEAD_ZONE = 0.45f;
    static final float MIN_RUMBLE_STRENGTH = 0.10f;
    static final float MAX_RUMBLE_STRENGTH = 1.00f;

    InputProfile {
        moveDeadZone = clampFinite(moveDeadZone, 0.20f, MIN_DEAD_ZONE, MAX_DEAD_ZONE);
        lookDeadZone = clampFinite(lookDeadZone, 0.16f, MIN_DEAD_ZONE, MAX_DEAD_ZONE);
        sprintMode = sprintMode == null ? SprintMode.HOLD : sprintMode;
        rumbleStrength = clampFinite(rumbleStrength, 0.60f, MIN_RUMBLE_STRENGTH, MAX_RUMBLE_STRENGTH);
        moveForwardKey = validKey(moveForwardKey, BindableAction.MOVE_FORWARD.defaultKey());
        moveBackwardKey = validKey(moveBackwardKey, BindableAction.MOVE_BACKWARD.defaultKey());
        moveLeftKey = validKey(moveLeftKey, BindableAction.MOVE_LEFT.defaultKey());
        moveRightKey = validKey(moveRightKey, BindableAction.MOVE_RIGHT.defaultKey());
        jumpKey = validKey(jumpKey, BindableAction.JUMP.defaultKey());
        interactKey = validKey(interactKey, BindableAction.INTERACT.defaultKey());
        mountKey = validKey(mountKey, BindableAction.MOUNT.defaultKey());
        buildKey = validKey(buildKey, BindableAction.BUILD.defaultKey());
        sprintKey = validKey(sprintKey, BindableAction.SPRINT.defaultKey());
        saveKey = validKey(saveKey, BindableAction.SAVE.defaultKey());
        pauseKey = validKey(pauseKey, BindableAction.PAUSE.defaultKey());
    }

    static InputProfile defaults() {
        return new InputProfile(
            false,
            0.20f,
            0.16f,
            SprintMode.HOLD,
            true,
            0.60f,
            BindableAction.MOVE_FORWARD.defaultKey(),
            BindableAction.MOVE_BACKWARD.defaultKey(),
            BindableAction.MOVE_LEFT.defaultKey(),
            BindableAction.MOVE_RIGHT.defaultKey(),
            BindableAction.JUMP.defaultKey(),
            BindableAction.INTERACT.defaultKey(),
            BindableAction.MOUNT.defaultKey(),
            BindableAction.BUILD.defaultKey(),
            BindableAction.SPRINT.defaultKey(),
            BindableAction.SAVE.defaultKey(),
            BindableAction.PAUSE.defaultKey()
        );
    }

    int keyFor(BindableAction action) {
        return switch (action) {
            case MOVE_FORWARD -> moveForwardKey;
            case MOVE_BACKWARD -> moveBackwardKey;
            case MOVE_LEFT -> moveLeftKey;
            case MOVE_RIGHT -> moveRightKey;
            case JUMP -> jumpKey;
            case INTERACT -> interactKey;
            case MOUNT -> mountKey;
            case BUILD -> buildKey;
            case SPRINT -> sprintKey;
            case SAVE -> saveKey;
            case PAUSE -> pauseKey;
        };
    }

    InputProfile withInvertCameraY(boolean value) {
        return copy(value, moveDeadZone, lookDeadZone, sprintMode, rumbleEnabled, rumbleStrength);
    }

    InputProfile withMoveDeadZone(float value) {
        return copy(invertCameraY, value, lookDeadZone, sprintMode, rumbleEnabled, rumbleStrength);
    }

    InputProfile withLookDeadZone(float value) {
        return copy(invertCameraY, moveDeadZone, value, sprintMode, rumbleEnabled, rumbleStrength);
    }

    InputProfile withSprintMode(SprintMode value) {
        return copy(invertCameraY, moveDeadZone, lookDeadZone, value, rumbleEnabled, rumbleStrength);
    }

    InputProfile withRumbleEnabled(boolean value) {
        return copy(invertCameraY, moveDeadZone, lookDeadZone, sprintMode, value, rumbleStrength);
    }

    InputProfile withRumbleStrength(float value) {
        return copy(invertCameraY, moveDeadZone, lookDeadZone, sprintMode, rumbleEnabled, value);
    }

    InputProfile withBinding(BindableAction action, int keyCode) {
        if (action == null || !isValidKey(keyCode)) return this;
        int oldKey = keyFor(action);
        BindableAction duplicate = actionForKey(keyCode);
        InputProfile updated = withRawBinding(action, keyCode);
        if (duplicate != null && duplicate != action) {
            updated = updated.withRawBinding(duplicate, oldKey);
        }
        return updated;
    }

    private BindableAction actionForKey(int keyCode) {
        for (BindableAction action : BindableAction.values()) {
            if (keyFor(action) == keyCode) return action;
        }
        return null;
    }

    private InputProfile withRawBinding(BindableAction action, int keyCode) {
        return new InputProfile(
            invertCameraY, moveDeadZone, lookDeadZone, sprintMode, rumbleEnabled, rumbleStrength,
            action == BindableAction.MOVE_FORWARD ? keyCode : moveForwardKey,
            action == BindableAction.MOVE_BACKWARD ? keyCode : moveBackwardKey,
            action == BindableAction.MOVE_LEFT ? keyCode : moveLeftKey,
            action == BindableAction.MOVE_RIGHT ? keyCode : moveRightKey,
            action == BindableAction.JUMP ? keyCode : jumpKey,
            action == BindableAction.INTERACT ? keyCode : interactKey,
            action == BindableAction.MOUNT ? keyCode : mountKey,
            action == BindableAction.BUILD ? keyCode : buildKey,
            action == BindableAction.SPRINT ? keyCode : sprintKey,
            action == BindableAction.SAVE ? keyCode : saveKey,
            action == BindableAction.PAUSE ? keyCode : pauseKey
        );
    }

    private InputProfile copy(
        boolean invert,
        float moveZone,
        float lookZone,
        SprintMode mode,
        boolean rumble,
        float strength
    ) {
        return new InputProfile(
            invert, moveZone, lookZone, mode, rumble, strength,
            moveForwardKey, moveBackwardKey, moveLeftKey, moveRightKey,
            jumpKey, interactKey, mountKey, buildKey, sprintKey, saveKey, pauseKey
        );
    }

    private static int validKey(int keyCode, int fallback) {
        return isValidKey(keyCode) ? keyCode : fallback;
    }

    private static boolean isValidKey(int keyCode) {
        return keyCode > 0 && keyCode <= 255;
    }

    private static float clampFinite(float value, float fallback, float min, float max) {
        if (!Float.isFinite(value)) value = fallback;
        return Math.max(min, Math.min(max, value));
    }
}
