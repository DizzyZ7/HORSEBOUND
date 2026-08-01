// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchCameraCollisionSystemTest {
    private final RanchCameraCollisionSystem system = new RanchCameraCollisionSystem();

    @Test
    void unobstructedCameraKeepsDesiredDistance() {
        float distance = system.resolveDistance(
            0f, 2f, 0f,
            0f, 5f, -10f,
            2f,
            List.of(),
            (x, z) -> 0f
        );

        assertEquals((float) Math.sqrt(109f), distance, 0.001f);
    }

    @Test
    void structureBetweenTargetAndCameraPullsCameraForward() {
        float desired = 10f;
        float distance = system.resolveDistance(
            0f, 2f, 0f,
            0f, 2f, -desired,
            2f,
            List.of(new RanchCameraCollisionSystem.Obstacle(0f, -5f, 1.2f, 4f)),
            (x, z) -> 0f
        );

        assertTrue(distance < 4f, "camera should stop before the obstacle");
        assertTrue(distance >= 2f, "minimum playable camera distance must be preserved");
    }

    @Test
    void risingTerrainClipsTheSegmentBeforeUndergroundCamera() {
        float distance = system.resolveDistance(
            0f, 2f, 0f,
            0f, 2f, -10f,
            1.5f,
            List.of(),
            (x, z) -> z < -4f ? 3f : 0f
        );

        assertTrue(distance < 4.5f);
        assertTrue(distance >= 1.5f);
    }

    @Test
    void invalidObstacleValuesNormalizeSafely() {
        RanchCameraCollisionSystem.Obstacle obstacle = new RanchCameraCollisionSystem.Obstacle(
            Float.NaN,
            Float.POSITIVE_INFINITY,
            -4f,
            Float.NaN
        );

        assertEquals(0f, obstacle.x());
        assertEquals(0f, obstacle.z());
        assertEquals(0f, obstacle.radius());
        assertEquals(0f, obstacle.height());
    }
}
