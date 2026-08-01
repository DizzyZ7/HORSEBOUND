// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Shared camera geometry for the provisional procedural tree and rock models. */
final class RanchNatureCameraObstacle {
    private RanchNatureCameraObstacle() {
    }

    static RanchCameraCollisionSystem.Obstacle tree(float x, float z, float scale) {
        float safeScale = safeScale(scale);
        return new RanchCameraCollisionSystem.Obstacle(x, z, 1.45f * safeScale, 5.70f * safeScale);
    }

    static RanchCameraCollisionSystem.Obstacle rock(float x, float z, float scale) {
        float safeScale = safeScale(scale);
        return new RanchCameraCollisionSystem.Obstacle(x, z, 0.90f * safeScale, 1.30f * safeScale);
    }

    private static float safeScale(float value) {
        if (!Float.isFinite(value)) return 1f;
        return Math.max(0.10f, Math.min(4f, value));
    }
}
