// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum GraphicsPreset {
    LOW("Low", 62f),
    MEDIUM("Medium", 94f),
    HIGH("High", 145f);

    private final String displayName;
    private final float objectDistance;

    GraphicsPreset(String displayName, float objectDistance) {
        this.displayName = displayName;
        this.objectDistance = objectDistance;
    }

    String displayName() {
        return displayName;
    }

    float objectDistance() {
        return objectDistance;
    }

    float objectDistanceSquared() {
        return objectDistance * objectDistance;
    }

    GraphicsPreset shifted(int direction) {
        GraphicsPreset[] values = values();
        return values[Math.floorMod(ordinal() + Integer.signum(direction), values.length)];
    }
}
