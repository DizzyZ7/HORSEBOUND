// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/**
 * Scoped bridge used while the legacy ranch presentation constructs its GameSession.
 * It avoids reflection for inventory, hotbar and Homestead ownership during 0.5 integration.
 */
final class GameSessionCapture {
    private static final ThreadLocal<CaptureState> STATE = ThreadLocal.withInitial(CaptureState::new);

    private GameSessionCapture() {
    }

    static void begin() {
        CaptureState state = STATE.get();
        if (state.armed) throw new IllegalStateException("A HORSEBOUND GameSession capture is already active.");
        state.armed = true;
        state.captured = null;
    }

    static void offer(GameSession session) {
        CaptureState state = STATE.get();
        if (state.armed && state.captured == null) state.captured = session;
    }

    static GameSession finish() {
        CaptureState state = STATE.get();
        GameSession result = state.captured;
        state.armed = false;
        state.captured = null;
        if (result == null) throw new IllegalStateException("LivingRanchScreen did not construct a GameSession.");
        return result;
    }

    static void cancel() {
        CaptureState state = STATE.get();
        state.armed = false;
        state.captured = null;
    }

    private static final class CaptureState {
        boolean armed;
        GameSession captured;
    }
}
