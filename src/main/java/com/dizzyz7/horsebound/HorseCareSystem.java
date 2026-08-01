// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

final class HorseCareSystem {
    private static final float SERVICE_RADIUS = 7.5f;
    private static final float STALL_RADIUS = 8.5f;

    CareResult update(
        HorseNeeds current,
        float seconds,
        boolean moving,
        boolean galloping,
        float horseX,
        float horseZ,
        HomesteadState homestead
    ) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(homestead, "homestead");

        HorseNeeds next = current.tick(seconds, moving, galloping);
        boolean fed = false;
        boolean watered = false;
        boolean rested = false;

        if (next.needsFeed() && homestead.consumeNearestResource(ItemId.HAY, horseX, horseZ, SERVICE_RADIUS)) {
            next = next.feed(35f);
            fed = true;
        }
        if (next.needsWater()
            && homestead.consumeNearestResource(ItemId.WATER_BUCKET, horseX, horseZ, SERVICE_RADIUS)) {
            next = next.water(42f);
            watered = true;
        }
        if (next.needsRest()
            && !moving
            && homestead.hasNearby(HomesteadStructureType.STALL, horseX, horseZ, STALL_RADIUS)) {
            next = next.rest(Math.max(0f, seconds) * 1.6f);
            rested = true;
        }

        return new CareResult(next, fed, watered, rested);
    }

    record CareResult(HorseNeeds needs, boolean fed, boolean watered, boolean rested) {
        CareResult {
            needs = Objects.requireNonNull(needs, "needs");
        }
    }
}
