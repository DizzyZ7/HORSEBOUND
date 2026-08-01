// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/**
 * Device-neutral gameplay intent. Keyboard, mouse, gamepad and Steam Input map into this contract.
 */
record PlayerCommand(
    float moveForward,
    float moveRight,
    float lookYaw,
    float lookPitch,
    boolean sprint,
    boolean jumpPressed,
    boolean interactPressed,
    boolean mountPressed,
    boolean buildPressed,
    boolean savePressed,
    boolean pausePressed
) {
    private static final float ACTIVITY_EPSILON = 0.015f;

    PlayerCommand {
        moveForward = clampAxis(moveForward);
        moveRight = clampAxis(moveRight);
        lookYaw = finiteOrZero(lookYaw);
        lookPitch = finiteOrZero(lookPitch);
    }

    static PlayerCommand idle() {
        return new PlayerCommand(0f, 0f, 0f, 0f, false, false, false, false, false, false, false);
    }

    boolean hasActivity() {
        return Math.abs(moveForward) > ACTIVITY_EPSILON
            || Math.abs(moveRight) > ACTIVITY_EPSILON
            || Math.abs(lookYaw) > ACTIVITY_EPSILON
            || Math.abs(lookPitch) > ACTIVITY_EPSILON
            || sprint
            || jumpPressed
            || interactPressed
            || mountPressed
            || buildPressed
            || savePressed
            || pausePressed;
    }

    PlayerCommand merge(PlayerCommand other) {
        if (other == null) return this;
        return new PlayerCommand(
            clampAxis(moveForward + other.moveForward),
            clampAxis(moveRight + other.moveRight),
            finiteOrZero(lookYaw + other.lookYaw),
            finiteOrZero(lookPitch + other.lookPitch),
            sprint || other.sprint,
            jumpPressed || other.jumpPressed,
            interactPressed || other.interactPressed,
            mountPressed || other.mountPressed,
            buildPressed || other.buildPressed,
            savePressed || other.savePressed,
            pausePressed || other.pausePressed
        );
    }

    PlayerCommand withoutPause() {
        if (!pausePressed) return this;
        return copy(interactPressed, buildPressed, false);
    }

    PlayerCommand withoutBuild() {
        if (!buildPressed) return this;
        return copy(interactPressed, false, pausePressed);
    }

    PlayerCommand withoutInteract() {
        if (!interactPressed) return this;
        return copy(false, buildPressed, pausePressed);
    }

    private PlayerCommand copy(boolean interact, boolean build, boolean pause) {
        return new PlayerCommand(
            moveForward,
            moveRight,
            lookYaw,
            lookPitch,
            sprint,
            jumpPressed,
            interact,
            mountPressed,
            build,
            savePressed,
            pause
        );
    }

    private static float clampAxis(float value) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(-1f, Math.min(1f, value));
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0f;
    }
}
