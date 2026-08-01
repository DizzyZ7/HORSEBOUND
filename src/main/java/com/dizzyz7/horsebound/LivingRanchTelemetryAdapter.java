// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.graphics.PerspectiveCamera;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Compatibility facade for the Homestead wrapper.
 * Since 0.5.3 it delegates to the explicit RanchWorldAccess contract and contains no reflection.
 */
final class LivingRanchTelemetryAdapter {
    private final RanchWorldAccess access;

    LivingRanchTelemetryAdapter(RanchWorldAccess access) {
        this.access = Objects.requireNonNull(access, "access");
    }

    PerspectiveCamera camera() {
        return access.camera();
    }

    ActorPose actorPose() {
        RanchWorldAccess.ActorPose pose = access.actorPose();
        return new ActorPose(pose.x(), pose.z(), pose.heading(), pose.mounted());
    }

    void setActorPosition(float x, float z) {
        access.setActorPosition(x, z);
    }

    boolean setHorsePosition(UUID id, float x, float z) {
        return access.setHorsePosition(id, x, z);
    }

    List<HorseTelemetry> horses() {
        return access.horses().stream()
            .map(value -> new HorseTelemetry(
                value.id(),
                value.x(),
                value.z(),
                value.speed(),
                value.mounted(),
                value.tamed()
            ))
            .toList();
    }

    record ActorPose(float x, float z, float heading, boolean mounted) {
    }

    record HorseTelemetry(UUID id, float x, float z, float speed, boolean mounted, boolean tamed) {
    }
}
