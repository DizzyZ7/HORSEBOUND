// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeGroundPlacementTest {
    private static final float WORLD_LIMIT = Terrain.WORLD_HALF_SIZE - 3f;

    @Test
    void keepsThePreferredPositionWhenItIsSafe() {
        SafeGroundPlacement.Position result = SafeGroundPlacement.nearest(5f, 5f, 7f, 5f, WORLD_LIMIT);

        assertEquals(7f, result.x());
        assertEquals(5f, result.z());
    }

    @Test
    void rejectsALakeSideDismountAndFindsDryGround() {
        float centerX = Terrain.LAKE_X;
        float centerZ = Terrain.LAKE_Z + 16f;
        float preferredX = Terrain.LAKE_X;
        float preferredZ = Terrain.LAKE_Z + 14f;

        SafeGroundPlacement.Position result = SafeGroundPlacement.nearest(
            centerX, centerZ, preferredX, preferredZ, WORLD_LIMIT
        );

        assertTrue(SafeGroundPlacement.isSafe(result.x(), result.z(), WORLD_LIMIT));
        assertFalse(result.x() == preferredX && result.z() == preferredZ);
    }

    @Test
    void clampsCandidatesAtTheWorldBoundary() {
        SafeGroundPlacement.Position result = SafeGroundPlacement.nearest(
            WORLD_LIMIT - 0.5f,
            0f,
            WORLD_LIMIT + 20f,
            0f,
            WORLD_LIMIT
        );

        assertTrue(Math.abs(result.x()) <= WORLD_LIMIT);
        assertTrue(Math.abs(result.z()) <= WORLD_LIMIT);
        assertTrue(SafeGroundPlacement.isSafe(result.x(), result.z(), WORLD_LIMIT));
    }

    @Test
    void sanitizesNonFiniteCoordinates() {
        SafeGroundPlacement.Position result = SafeGroundPlacement.nearest(
            Float.NaN, Float.POSITIVE_INFINITY, Float.NaN, Float.NEGATIVE_INFINITY, WORLD_LIMIT
        );

        assertTrue(Float.isFinite(result.x()));
        assertTrue(Float.isFinite(result.z()));
        assertTrue(SafeGroundPlacement.isSafe(result.x(), result.z(), WORLD_LIMIT));
    }
}
