// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchWorldAccessTest {
    @Test
    void typedContractProvidesActorHorseAndCameraObstacleAccess() {
        UUID horseId = UUID.fromString("c7255a15-8a09-49d2-abd9-b455207f25d4");
        FakeAccess access = new FakeAccess(horseId);

        assertNull(access.camera());
        assertEquals(new RanchWorldAccess.ActorPose(2f, 3f, 45f, false), access.actorPose());
        assertEquals(horseId, access.horses().getFirst().id());

        access.setActorPosition(8f, 9f);
        assertEquals(8f, access.actorX);
        assertEquals(9f, access.actorZ);
        assertTrue(access.setHorsePosition(horseId, 11f, 12f));
        assertEquals(11f, access.horseX);
        assertEquals(12f, access.horseZ);
        assertFalse(access.setHorsePosition(UUID.randomUUID(), 1f, 1f));

        List<RanchCameraCollisionSystem.Obstacle> mutable = new ArrayList<>();
        mutable.add(new RanchCameraCollisionSystem.Obstacle(4f, 5f, 1f, 2f));
        access.setCameraObstacles(mutable);
        mutable.clear();
        assertEquals(1, access.cameraObstacles.size());
    }

    @Test
    void compatibilityFacadeIsPhysicallyRemovedAndHomesteadUsesTypedContract() throws Exception {
        Path adapter = Path.of(
            "src/main/java/com/dizzyz7/horsebound/LivingRanchTelemetryAdapter.java"
        );
        String homestead = Files.readString(Path.of(
            "src/main/java/com/dizzyz7/horsebound/HomesteadRanchScreen.java"
        ));

        assertFalse(Files.exists(adapter));
        assertFalse(homestead.contains("LivingRanchTelemetryAdapter"));
        assertTrue(homestead.contains("RanchWorldAccess"));
    }

    private static final class FakeAccess implements RanchWorldAccess {
        private final UUID horseId;
        private float actorX = 2f;
        private float actorZ = 3f;
        private float horseX = 5f;
        private float horseZ = 6f;
        private List<RanchCameraCollisionSystem.Obstacle> cameraObstacles = List.of();

        private FakeAccess(UUID horseId) {
            this.horseId = horseId;
        }

        @Override
        public PerspectiveCamera camera() {
            return null;
        }

        @Override
        public ActorPose actorPose() {
            return new ActorPose(actorX, actorZ, 45f, false);
        }

        @Override
        public List<HorseTelemetry> horses() {
            return List.of(new HorseTelemetry(horseId, horseX, horseZ, 0.5f, false, true));
        }

        @Override
        public void setActorPosition(float x, float z) {
            actorX = x;
            actorZ = z;
        }

        @Override
        public boolean setHorsePosition(UUID id, float x, float z) {
            if (!horseId.equals(id)) return false;
            horseX = x;
            horseZ = z;
            return true;
        }

        @Override
        public void setCameraObstacles(List<RanchCameraCollisionSystem.Obstacle> obstacles) {
            cameraObstacles = obstacles == null ? List.of() : List.copyOf(obstacles);
        }
    }
}
