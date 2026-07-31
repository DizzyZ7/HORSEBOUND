// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSimulationLoopTest {
    @Test
    void samplesOncePerFrameAndDeliversEdgeActionOnlyOnFirstCatchUpTick() {
        GameSession session = new GameSession(SaveGame.fresh(new WorldSeed(42L)));
        AtomicInteger samples = new AtomicInteger();
        InputMapper mapper = () -> {
            samples.incrementAndGet();
            return new InputSnapshot(
                new PlayerCommand(1f, 0f, 0f, 0f, true, true, false, false, false, false, false),
                InputDeviceType.GAMEPAD
            );
        };
        GameSimulationLoop loop = new GameSimulationLoop(session, mapper);
        List<PlayerCommand> delivered = new ArrayList<>();

        int steps = loop.advance(1f / 30f, (dt, command) -> delivered.add(command));

        assertEquals(1, samples.get());
        assertEquals(2, steps);
        assertEquals(2, delivered.size());
        assertTrue(delivered.get(0).jumpPressed());
        assertFalse(delivered.get(1).jumpPressed());
        assertEquals(1f, delivered.get(0).moveForward());
        assertEquals(1f, delivered.get(1).moveForward());
        assertEquals(InputDeviceType.GAMEPAD, loop.activeDevice());
    }

    @Test
    void retainsEdgeActionWhenFrameDoesNotYetProduceSimulationTick() {
        GameSession session = new GameSession(SaveGame.fresh(new WorldSeed(7L)));
        AtomicInteger sample = new AtomicInteger();
        InputMapper mapper = () -> sample.getAndIncrement() == 0
            ? new InputSnapshot(
                new PlayerCommand(0f, 0f, 0f, 0f, false, false, true, false, false, false, false),
                InputDeviceType.STEAM_INPUT
            )
            : InputSnapshot.idle();
        GameSimulationLoop loop = new GameSimulationLoop(session, mapper);
        List<PlayerCommand> delivered = new ArrayList<>();

        assertEquals(0, loop.advance(1f / 120f, (dt, command) -> delivered.add(command)));
        assertEquals(1, loop.advance(1f / 120f, (dt, command) -> delivered.add(command)));

        assertEquals(1, delivered.size());
        assertTrue(delivered.getFirst().interactPressed());
    }

    @Test
    void exposesInterpolationAndCanResetPendingInput() {
        GameSession session = new GameSession(SaveGame.fresh(new WorldSeed(9L)));
        InputMapper mapper = () -> new InputSnapshot(
            new PlayerCommand(1f, 0f, 0f, 0f, true, true, true, true, true, true, true),
            InputDeviceType.KEYBOARD_MOUSE
        );
        GameSimulationLoop loop = new GameSimulationLoop(session, mapper);

        loop.advance(1f / 120f, (dt, command) -> { });
        assertTrue(loop.interpolationAlpha() > 0f);
        loop.resetInput();

        List<PlayerCommand> delivered = new ArrayList<>();
        GameSimulationLoop idleLoop = new GameSimulationLoop(session, InputSnapshot::idle);
        idleLoop.advance(1f / 120f, (dt, command) -> delivered.add(command));
        assertEquals(PlayerCommand.idle(), delivered.getFirst());
    }
}
