// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchCameraFadeSystemTest {
    private final RanchCameraFadeSystem fade = new RanchCameraFadeSystem();

    @Test
    void obstacleDirectlyBetweenTargetAndCameraFadesStrongly() {
        RanchCameraCollisionSystem.Obstacle obstacle = new RanchCameraCollisionSystem.Obstacle(0f, 4f, 1f, 4f);

        float alpha = fade.alphaFor(obstacle, 0f, 0f, 1.5f, 0f, 0f, 3f, 8f);

        assertTrue(alpha <= 0.35f);
        assertTrue(alpha >= RanchCameraFadeSystem.MIN_ALPHA);
    }

    @Test
    void obstacleOutsideCameraSegmentRemainsOpaque() {
        RanchCameraCollisionSystem.Obstacle behindCamera = new RanchCameraCollisionSystem.Obstacle(0f, 12f, 1f, 4f);
        RanchCameraCollisionSystem.Obstacle offAxis = new RanchCameraCollisionSystem.Obstacle(5f, 4f, 1f, 4f);

        assertEquals(1f, fade.alphaFor(behindCamera, 0f, 0f, 1.5f, 0f, 0f, 3f, 8f));
        assertEquals(1f, fade.alphaFor(offAxis, 0f, 0f, 1.5f, 0f, 0f, 3f, 8f));
    }

    @Test
    void fadeTransitionsSmoothlyNearTheOcclusionMargin() {
        RanchCameraCollisionSystem.Obstacle close = new RanchCameraCollisionSystem.Obstacle(0.9f, 4f, 0.4f, 2.5f);
        RanchCameraCollisionSystem.Obstacle farther = new RanchCameraCollisionSystem.Obstacle(1.35f, 4f, 0.4f, 2.5f);

        float closeAlpha = fade.alphaFor(close, 0f, 0f, 1.5f, 0f, 0f, 3f, 8f);
        float fartherAlpha = fade.alphaFor(farther, 0f, 0f, 1.5f, 0f, 0f, 3f, 8f);

        assertTrue(closeAlpha < fartherAlpha);
        assertTrue(fartherAlpha <= 1f);
    }
}
