// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Arrays;

enum DisplayResolution {
    HD_720(1280, 720),
    DECK_800(1280, 800),
    HD_PLUS(1600, 900),
    FULL_HD(1920, 1080);

    private final int width;
    private final int height;

    DisplayResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    int width() {
        return width;
    }

    int height() {
        return height;
    }

    String displayName() {
        return width + " x " + height;
    }

    DisplayResolution shifted(int direction) {
        DisplayResolution[] values = values();
        int current = ordinal();
        return values[Math.floorMod(current + Integer.signum(direction), values.length)];
    }

    static DisplayResolution closest(int width, int height) {
        return Arrays.stream(values())
            .min((left, right) -> Integer.compare(
                distance(left, width, height),
                distance(right, width, height)
            ))
            .orElse(DECK_800);
    }

    private static int distance(DisplayResolution resolution, int width, int height) {
        return Math.abs(resolution.width - width) + Math.abs(resolution.height - height);
    }
}
