// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum GraphicsPreset {
    LOW("Low", 0, 60, 62f),
    MEDIUM("Medium", 2, 90, 94f),
    HIGH("High", 4, 144, 145f);

    private final String displayName;
    private final int msaaSamples;
    private final int foregroundFps;
    private final float objectDistance;

    GraphicsPreset(String displayName, int msaaSamples, int foregroundFps, float objectDistance) {
        this.displayName = displayName;
        this.msaaSamples = msaaSamples;
        this.foregroundFps = foregroundFps;
        this.objectDistance = objectDistance;
    }

    String displayName() {
        return displayName;
    }

    int msaaSamples() {
        return msaaSamples;
    }

    int foregroundFps() {
        return foregroundFps;
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
