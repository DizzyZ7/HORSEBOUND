// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Deterministic safe-ground search for dismounts and companion catch-up placement. */
final class SafeGroundPlacement {
    private static final float[] ANGLE_OFFSETS = {0f, 180f, 90f, -90f, 45f, -45f, 135f, -135f};

    private SafeGroundPlacement() {
    }

    static Position nearest(
        float centerX,
        float centerZ,
        float preferredX,
        float preferredZ,
        float worldLimit
    ) {
        float safeLimit = Float.isFinite(worldLimit)
            ? Math.max(1f, Math.abs(worldLimit))
            : Terrain.WORLD_HALF_SIZE - 3f;
        float safeCenterX = clampFinite(centerX, safeLimit);
        float safeCenterZ = clampFinite(centerZ, safeLimit);
        float safePreferredX = clampFinite(preferredX, safeLimit);
        float safePreferredZ = clampFinite(preferredZ, safeLimit);

        if (isSafe(safePreferredX, safePreferredZ, safeLimit)) {
            return new Position(safePreferredX, safePreferredZ);
        }

        float dx = safePreferredX - safeCenterX;
        float dz = safePreferredZ - safeCenterZ;
        float preferredDistance = (float) Math.sqrt(dx * dx + dz * dz);
        float baseAngle = preferredDistance > 0.001f
            ? (float) Math.toDegrees(Math.atan2(dx, dz))
            : 0f;
        float[] radii = {
            Math.max(1.25f, preferredDistance),
            Math.max(2.25f, preferredDistance + 0.75f),
            Math.max(3.25f, preferredDistance + 1.75f),
            4.75f
        };
        for (float radius : radii) {
            for (float angleOffset : ANGLE_OFFSETS) {
                double radians = Math.toRadians(baseAngle + angleOffset);
                float candidateX = clampFinite(
                    safeCenterX + (float) Math.sin(radians) * radius,
                    safeLimit
                );
                float candidateZ = clampFinite(
                    safeCenterZ + (float) Math.cos(radians) * radius,
                    safeLimit
                );
                if (isSafe(candidateX, candidateZ, safeLimit)) {
                    return new Position(candidateX, candidateZ);
                }
            }
        }

        if (isSafe(safeCenterX, safeCenterZ, safeLimit)) {
            return new Position(safeCenterX, safeCenterZ);
        }

        for (float radius = 2f; radius <= 24f; radius += 2f) {
            for (int step = 0; step < 16; step++) {
                double radians = Math.toRadians(step * 22.5f);
                float candidateX = clampFinite(
                    safeCenterX + (float) Math.sin(radians) * radius,
                    safeLimit
                );
                float candidateZ = clampFinite(
                    safeCenterZ + (float) Math.cos(radians) * radius,
                    safeLimit
                );
                if (isSafe(candidateX, candidateZ, safeLimit)) {
                    return new Position(candidateX, candidateZ);
                }
            }
        }
        return new Position(0f, 0f);
    }

    static boolean isSafe(float x, float z, float worldLimit) {
        return Float.isFinite(x)
            && Float.isFinite(z)
            && Math.abs(x) <= worldLimit
            && Math.abs(z) <= worldLimit
            && !Terrain.isInsideLake(x, z);
    }

    private static float clampFinite(float value, float limit) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(-limit, Math.min(limit, value));
    }

    record Position(float x, float z) {
        Position {
            x = Float.isFinite(x) ? x : 0f;
            z = Float.isFinite(z) ? z : 0f;
        }
    }
}
