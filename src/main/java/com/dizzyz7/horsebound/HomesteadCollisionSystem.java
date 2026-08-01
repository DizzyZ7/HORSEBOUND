// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.List;

/** Pure-Java swept-circle collision against placed Homestead structures. */
final class HomesteadCollisionSystem {
    private static final float MAX_STEP = 0.22f;
    private static final float SCORE_EPSILON = 0.0001f;
    private static final float PUSH_MARGIN = 0.015f;

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
        float startScore = penetrationScore(startX, startZ, safeRadius, safeStructures);
        if (distance <= SCORE_EPSILON && startScore > SCORE_EPSILON) {
            Position pushed = pushOut(startX, startZ, safeRadius, safeStructures);
            return new Position(pushed.x(), pushed.z(), true);
        }

        int steps = Math.max(1, (int) Math.ceil(distance / MAX_STEP));
        float lastX = startX;
        float lastZ = startZ;
        float lastScore = startScore;
        boolean escapingInitialOverlap = lastScore > SCORE_EPSILON;

        for (int i = 1; i <= steps; i++) {
            float alpha = i / (float) steps;
            float candidateX = startX + dx * alpha;
            float candidateZ = startZ + dz * alpha;
            float candidateScore = penetrationScore(candidateX, candidateZ, safeRadius, safeStructures);

            if (escapingInitialOverlap) {
                if (candidateScore > lastScore + SCORE_EPSILON) {
                    return new Position(lastX, lastZ, true);
                }
                lastX = candidateX;
                lastZ = candidateZ;
                lastScore = candidateScore;
                if (candidateScore <= SCORE_EPSILON) escapingInitialOverlap = false;
                continue;
            }

            if (candidateScore > SCORE_EPSILON) return new Position(lastX, lastZ, true);
            lastX = candidateX;
            lastZ = candidateZ;
        }
        return new Position(endX, endZ, false);
    }

    boolean collides(float x, float z, float actorRadius, List<PlacedStructure> structures) {
        return penetrationScore(
            finite(x),
            finite(z),
            Float.isFinite(actorRadius) ? Math.max(0f, actorRadius) : 0f,
            structures == null ? List.of() : structures
        ) > SCORE_EPSILON;
    }

    private static Position pushOut(
        float startX,
        float startZ,
        float actorRadius,
        List<PlacedStructure> structures
    ) {
        float x = startX;
        float z = startZ;
        for (int pass = 0; pass < 4; pass++) {
            boolean moved = false;
            for (PlacedStructure structure : structures) {
                if (structure == null || !structure.blocksMovement()) continue;
                float required = actorRadius + structure.type().collisionRadius();
                float dx = x - structure.x();
                float dz = z - structure.z();
                float distanceSquared = dx * dx + dz * dz;
                if (distanceSquared >= required * required) continue;
                float distance = (float) Math.sqrt(distanceSquared);
                if (distance <= SCORE_EPSILON) {
                    dx = deterministicDirection(structure.id().getLeastSignificantBits());
                    dz = deterministicDirection(structure.id().getMostSignificantBits());
                    float length = (float) Math.sqrt(dx * dx + dz * dz);
                    dx /= length;
                    dz /= length;
                    distance = 0f;
                } else {
                    dx /= distance;
                    dz /= distance;
                }
                float correction = required - distance + PUSH_MARGIN;
                x += dx * correction;
                z += dz * correction;
                moved = true;
            }
            if (!moved) break;
        }
        return new Position(x, z, true);
    }

    private static float penetrationScore(
        float x,
        float z,
        float actorRadius,
        List<PlacedStructure> structures
    ) {
        float score = 0f;
        for (PlacedStructure structure : structures) {
            if (structure == null || !structure.blocksMovement()) continue;
            float required = actorRadius + structure.type().collisionRadius();
            float dx = x - structure.x();
            float dz = z - structure.z();
            float overlap = required * required - (dx * dx + dz * dz);
            if (overlap > 0f) score += overlap;
        }
        return score;
    }

    private static float deterministicDirection(long bits) {
        return (bits & 1L) == 0L ? 1f : -1f;
    }

    private static float finite(float value) {
        return Float.isFinite(value) ? value : 0f;
    }

    record Position(float x, float z, boolean blocked) {
    }
}
