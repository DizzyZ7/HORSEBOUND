// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class PushikMind {
    private PushikState state = PushikState.FOLLOW;
    private float stateTime;
    private float affection = 45f;

    PushikState state() {
        return state;
    }

    float affection() {
        return affection;
    }

    void tick(float dt, float distanceToPlayer, float worldTime) {
        stateTime += Math.max(0f, dt);

        if (distanceToPlayer > 7.5f) {
            switchState(PushikState.FOLLOW);
            return;
        }

        boolean night = worldTime > 0.82f || worldTime < 0.18f;
        if (night && distanceToPlayer < 3.5f && state != PushikState.GREET) {
            switchState(PushikState.SLEEP);
            return;
        }

        if (state == PushikState.GREET && stateTime < 4.5f) {
            return;
        }

        if (stateTime > 9f) {
            if (state == PushikState.SIT) {
                switchState(PushikState.EXPLORE);
            } else if (state == PushikState.EXPLORE) {
                switchState(PushikState.SIT);
            } else {
                switchState(affection >= 65f ? PushikState.SIT : PushikState.EXPLORE);
            }
        }
    }

    void pet() {
        affection = Math.min(100f, affection + 6f);
        switchState(PushikState.GREET);
    }

    void reunited() {
        affection = Math.min(100f, affection + 1f);
        switchState(PushikState.GREET);
    }

    private void switchState(PushikState next) {
        if (state != next) {
            state = next;
            stateTime = 0f;
        }
    }
}
