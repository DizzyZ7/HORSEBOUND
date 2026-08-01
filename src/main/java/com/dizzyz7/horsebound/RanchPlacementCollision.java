// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Pure circle-overlap checks shared by building validation and world-owned props. */
final class RanchPlacementCollision {
    private RanchPlacementCollision() {
    }

    static boolean overlaps(
        float firstX,
        float firstZ,
        float firstRadius,
        float secondX,
        float secondZ,
        float secondRadius,
        float clearance
    ) {
        if (!Float.isFinite(firstX) || !Float.isFinite(firstZ)
            || !Float.isFinite(secondX) || !Float.isFinite(secondZ)) {
            return true;
        }
        float radius = safeRadius(firstRadius) + safeRadius(secondRadius) + safeRadius(clearance);
        float dx = firstX - secondX;
        float dz = firstZ - secondZ;
        return dx * dx + dz * dz < radius * radius;
    }

    private static float safeRadius(float value) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(0f, value);
    }
}
