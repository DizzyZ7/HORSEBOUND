// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

/**
 * Samples physical input once per rendered frame and delivers device-neutral intent to fixed simulation ticks.
 */
final class GameSimulationLoop {
    private final GameSession session;
    private final InputMapper inputMapper;
    private final PlayerCommandBuffer commandBuffer = new PlayerCommandBuffer();
    private InputDeviceType activeDevice = InputDeviceType.KEYBOARD_MOUSE;

    GameSimulationLoop(GameSession session, InputMapper inputMapper) {
        this.session = Objects.requireNonNull(session, "session");
        this.inputMapper = Objects.requireNonNull(inputMapper, "inputMapper");
    }

    int advance(float frameDeltaSeconds, GameplayStep gameplayStep) {
        Objects.requireNonNull(gameplayStep, "gameplayStep");
        InputSnapshot input = Objects.requireNonNullElse(inputMapper.sample(), InputSnapshot.idle());
        activeDevice = input.activeDevice();
        commandBuffer.submit(input.command());

        return session.advanceSimulation(frameDeltaSeconds, fixedDeltaSeconds ->
            gameplayStep.update(fixedDeltaSeconds, commandBuffer.consumeForSimulationStep())
        );
    }

    InputDeviceType activeDevice() {
        return activeDevice;
    }

    float interpolationAlpha() {
        return session.simulationInterpolationAlpha();
    }

    void resetInput() {
        commandBuffer.reset();
    }
}
