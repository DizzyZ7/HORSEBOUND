// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RanchNatureCameraObstacleTest {
    @Test
    void treeAndRockGeometryScaleWithVisibleModel() {
        RanchCameraCollisionSystem.Obstacle tree = RanchNatureCameraObstacle.tree(3f, 4f, 1.5f);
        RanchCameraCollisionSystem.Obstacle rock = RanchNatureCameraObstacle.rock(-2f, 7f, 0.5f);

        assertEquals(3f, tree.x());
        assertEquals(4f, tree.z());
        assertEquals(1.45f * 1.5f, tree.radius());
        assertEquals(5.70f * 1.5f, tree.height());
        assertEquals(0.90f * 0.5f, rock.radius());
        assertEquals(1.30f * 0.5f, rock.height());
    }

    @Test
    void invalidScalesFallBackAndExtremeScalesAreBounded() {
        RanchCameraCollisionSystem.Obstacle invalid = RanchNatureCameraObstacle.tree(0f, 0f, Float.NaN);
        RanchCameraCollisionSystem.Obstacle huge = RanchNatureCameraObstacle.rock(0f, 0f, 100f);

        assertEquals(1.45f, invalid.radius());
        assertEquals(5.70f, invalid.height());
        assertEquals(0.90f * 4f, huge.radius());
        assertEquals(1.30f * 4f, huge.height());
        assertTrue(huge.height() > huge.radius());
    }
}
