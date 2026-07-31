// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/**
 * Bridges render-rate input sampling and fixed-rate simulation.
 * Continuous intent remains active while edge actions are delivered exactly once.
 */
final class PlayerCommandBuffer {
    private float moveForward;
    private float moveRight;
    private float pendingLookYaw;
    private float pendingLookPitch;
    private boolean sprint;
    private boolean jumpPressed;
    private boolean interactPressed;
    private boolean mountPressed;
    private boolean buildPressed;
    private boolean savePressed;
    private boolean pausePressed;

    void submit(PlayerCommand command) {
        if (command == null) return;
        moveForward = command.moveForward();
        moveRight = command.moveRight();
        pendingLookYaw += command.lookYaw();
        pendingLookPitch += command.lookPitch();
        sprint = command.sprint();
        jumpPressed |= command.jumpPressed();
        interactPressed |= command.interactPressed();
        mountPressed |= command.mountPressed();
        buildPressed |= command.buildPressed();
        savePressed |= command.savePressed();
        pausePressed |= command.pausePressed();
    }

    PlayerCommand consumeForSimulationStep() {
        PlayerCommand command = new PlayerCommand(
            moveForward,
            moveRight,
            pendingLookYaw,
            pendingLookPitch,
            sprint,
            jumpPressed,
            interactPressed,
            mountPressed,
            buildPressed,
            savePressed,
            pausePressed
        );
        pendingLookYaw = 0f;
        pendingLookPitch = 0f;
        jumpPressed = false;
        interactPressed = false;
        mountPressed = false;
        buildPressed = false;
        savePressed = false;
        pausePressed = false;
        return command;
    }

    void reset() {
        moveForward = 0f;
        moveRight = 0f;
        pendingLookYaw = 0f;
        pendingLookPitch = 0f;
        sprint = false;
        jumpPressed = false;
        interactPressed = false;
        mountPressed = false;
        buildPressed = false;
        savePressed = false;
        pausePressed = false;
    }
}
