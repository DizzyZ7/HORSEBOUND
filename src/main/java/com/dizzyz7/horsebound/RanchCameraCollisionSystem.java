// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.List;

/** Pure-Java segment sampling for third-person camera terrain and structure avoidance. */
final class RanchCameraCollisionSystem {
    private static final float SAMPLE_SPACING = 0.20f;
    private static final float TERRAIN_CLEARANCE = 0.32f;
    private static final float COLLISION_BACKOFF = 0.12f;
    private static final int MAX_SAMPLES = 128;

    float resolveDistance(
        float targetX,
        float targetY,
        float targetZ,
        float desiredX,
        float desiredY,
        float desiredZ,
        float minimumDistance,
        List<Obstacle> obstacles,
        HeightSampler terrain
    ) {
        float safeTargetX = finiteOrZero(targetX);
        float safeTargetY = finiteOrZero(targetY);
        float safeTargetZ = finiteOrZero(targetZ);
        float safeDesiredX = finiteOrZero(desiredX);
        float safeDesiredY = finiteOrZero(desiredY);
        float safeDesiredZ = finiteOrZero(desiredZ);
        HeightSampler safeTerrain = terrain == null ? (x, z) -> 0f : terrain;
        List<Obstacle> safeObstacles = obstacles == null ? List.of() : obstacles;

        float dx = safeDesiredX - safeTargetX;
        float dy = safeDesiredY - safeTargetY;
        float dz = safeDesiredZ - safeTargetZ;
        float desiredDistance = length(dx, dy, dz);
        if (desiredDistance <= 0.001f) return 0f;

        float minimum = clamp(finiteOrZero(minimumDistance), 0f, desiredDistance);
        int samples = Math.max(1, Math.min(MAX_SAMPLES, (int) Math.ceil(desiredDistance / SAMPLE_SPACING)));
        float lastSafeDistance = minimum;

        for (int i = 1; i <= samples; i++) {
            float distance = Math.max(minimum, desiredDistance * i / samples);
            float t = distance / desiredDistance;
            float x = safeTargetX + dx * t;
            float y = safeTargetY + dy * t;
            float z = safeTargetZ + dz * t;
            if (collidesTerrain(x, y, z, safeTerrain) || collidesObstacle(x, y, z, safeObstacles, safeTerrain)) {
                return Math.max(minimum, lastSafeDistance - COLLISION_BACKOFF);
            }
            lastSafeDistance = distance;
        }
        return desiredDistance;
    }

    private static boolean collidesTerrain(float x, float y, float z, HeightSampler terrain) {
        float ground = finiteOrZero(terrain.heightAt(x, z));
        return y < ground + TERRAIN_CLEARANCE;
    }

    private static boolean collidesObstacle(
        float x,
        float y,
        float z,
        List<Obstacle> obstacles,
        HeightSampler terrain
    ) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle == null || obstacle.radius() <= 0f || obstacle.height() <= 0f) continue;
            float ox = x - obstacle.x();
            float oz = z - obstacle.z();
            if (ox * ox + oz * oz > obstacle.radius() * obstacle.radius()) continue;
            float base = finiteOrZero(terrain.heightAt(obstacle.x(), obstacle.z()));
            if (y >= base - 0.15f && y <= base + obstacle.height()) return true;
        }
        return false;
    }

    private static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0f;
    }

    record Obstacle(float x, float z, float radius, float height) {
        Obstacle {
            x = finiteOrZero(x);
            z = finiteOrZero(z);
            radius = Math.max(0f, finiteOrZero(radius));
            height = Math.max(0f, finiteOrZero(height));
        }
    }

    @FunctionalInterface
    interface HeightSampler {
        float heightAt(float x, float z);
    }
}
