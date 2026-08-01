// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.List;

/** Pure-Java swept-circle collision against placed Homestead structures. */
final class HomesteadCollisionSystem {
    private static final float MAX_STEP = 0.22f;

    Position resolve(
        float previousX,
        float previousZ,
        float desiredX,
        float desiredZ,
        float actorRadius,
        List<PlacedStructure> structures
    ) {
        float startX = finite(previousX);
        float startZ = finite(previousZ);
        float endX = finite(desiredX);
        float endZ = finite(desiredZ);
        float safeRadius = Float.isFinite(actorRadius) ? Math.max(0f, actorRadius) : 0f;
        List<PlacedStructure> safeStructures = structures == null ? List.of() : structures;

        float dx = endX - startX;
        float dz = endZ - startZ;
        float distance = (float) Math.sqrt(dx * dx + dz * dz);
        int steps = Math.max(1, (int) Math.ceil(distance / MAX_STEP));
        float lastX = startX;
        float lastZ = startZ;

        for (int i = 1; i <= steps; i++) {
            float alpha = i / (float) steps;
            float candidateX = startX + dx * alpha;
            float candidateZ = startZ + dz * alpha;
            if (collides(candidateX, candidateZ, safeRadius, safeStructures)) {
                return new Position(lastX, lastZ, true);
            }
            lastX = candidateX;
            lastZ = candidateZ;
        }
        return new Position(endX, endZ, false);
    }

    boolean collides(float x, float z, float actorRadius, List<PlacedStructure> structures) {
        float safeX = finite(x);
        float safeZ = finite(z);
        float safeRadius = Float.isFinite(actorRadius) ? Math.max(0f, actorRadius) : 0f;
        if (structures == null) return false;
        for (PlacedStructure structure : structures) {
            if (structure == null || !structure.blocksMovement()) continue;
            float required = safeRadius + structure.type().collisionRadius();
            float dx = safeX - structure.x();
            float dz = safeZ - structure.z();
            if (dx * dx + dz * dz < required * required) return true;
        }
        return false;
    }

    private static float finite(float value) {
        return Float.isFinite(value) ? value : 0f;
    }

    record Position(float x, float z, boolean blocked) {
    }
}
