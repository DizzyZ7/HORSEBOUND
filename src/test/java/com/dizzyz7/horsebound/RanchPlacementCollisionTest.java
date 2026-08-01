// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchPlacementCollisionTest {
    @Test
    void detectsCombinedRadiusOverlap() {
        assertTrue(RanchPlacementCollision.overlaps(0f, 0f, 1f, 2f, 0f, 1f, 0.1f));
        assertFalse(RanchPlacementCollision.overlaps(0f, 0f, 1f, 2.2f, 0f, 1f, 0.1f));
    }

    @Test
    void rejectsNonFinitePlacementCoordinates() {
        assertTrue(RanchPlacementCollision.overlaps(Float.NaN, 0f, 1f, 5f, 5f, 1f, 0f));
    }
}
