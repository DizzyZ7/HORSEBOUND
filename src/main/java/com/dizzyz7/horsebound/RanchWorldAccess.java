// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.graphics.PerspectiveCamera;

import java.util.List;
import java.util.UUID;

/**
 * Narrow presentation boundary exposed by the ranch renderer to Homestead orchestration.
 * Domain code never depends on this interface; it exists only for typed render-thread access.
 */
interface RanchWorldAccess {
    PerspectiveCamera camera();

    ActorPose actorPose();

    List<HorseTelemetry> horses();

    void setActorPosition(float x, float z);

    boolean setHorsePosition(UUID horseId, float x, float z);

    record ActorPose(float x, float z, float heading, boolean mounted) {
        ActorPose {
            x = finiteOrZero(x);
            z = finiteOrZero(z);
            heading = finiteOrZero(heading);
        }
    }

    record HorseTelemetry(UUID id, float x, float z, float speed, boolean mounted, boolean tamed) {
        HorseTelemetry {
            if (id == null) throw new IllegalArgumentException("horse id is required");
            x = finiteOrZero(x);
            z = finiteOrZero(z);
            speed = finiteOrZero(speed);
        }
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0f;
    }
}
