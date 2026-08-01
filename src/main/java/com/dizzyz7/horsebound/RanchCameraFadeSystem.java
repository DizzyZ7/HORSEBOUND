// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Pure geometry for soft-fading world props that sit between the actor target and camera. */
final class RanchCameraFadeSystem {
    static final float MIN_ALPHA = 0.18f;
    private static final float FADE_MARGIN = 0.85f;

    float alphaFor(
        RanchCameraCollisionSystem.Obstacle obstacle,
        float obstacleBaseY,
        float targetX,
        float targetY,
        float targetZ,
        float cameraX,
        float cameraY,
        float cameraZ
    ) {
        if (obstacle == null) return 1f;
        float dx = cameraX - targetX;
        float dy = cameraY - targetY;
        float dz = cameraZ - targetZ;
        float lengthSquared = dx * dx + dy * dy + dz * dz;
        if (!Float.isFinite(lengthSquared) || lengthSquared <= 0.0001f) return 1f;

        float centerX = obstacle.x();
        float centerY = finiteOrZero(obstacleBaseY) + obstacle.height() * 0.55f;
        float centerZ = obstacle.z();
        float t = ((centerX - targetX) * dx + (centerY - targetY) * dy + (centerZ - targetZ) * dz)
            / lengthSquared;
        if (!Float.isFinite(t) || t <= 0.04f || t >= 1.04f) return 1f;

        float closestX = targetX + dx * t;
        float closestY = targetY + dy * t;
        float closestZ = targetZ + dz * t;
        float offsetX = centerX - closestX;
        float offsetY = centerY - closestY;
        float offsetZ = centerZ - closestZ;
        float distance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
        float effectiveRadius = Math.max(obstacle.radius(), obstacle.height() * 0.22f);
        float clearance = distance - effectiveRadius;
        if (clearance >= FADE_MARGIN) return 1f;
        if (clearance <= 0f) return MIN_ALPHA;
        float normalized = clearance / FADE_MARGIN;
        return MIN_ALPHA + (1f - MIN_ALPHA) * normalized * normalized;
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0f;
    }
}
