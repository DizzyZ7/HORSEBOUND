// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum GraphicsPreset {
    LOW("graphics.low", 0, 60, 62f),
    MEDIUM("graphics.medium", 2, 90, 94f),
    HIGH("graphics.high", 4, 144, 145f);

    private final String displayKey;
    private final int msaaSamples;
    private final int foregroundFps;
    private final float objectDistance;

    GraphicsPreset(String displayKey, int msaaSamples, int foregroundFps, float objectDistance) {
        this.displayKey = displayKey;
        this.msaaSamples = msaaSamples;
        this.foregroundFps = foregroundFps;
        this.objectDistance = objectDistance;
    }

    String displayName() { return I18n.text(displayKey); }
    int msaaSamples() { return msaaSamples; }
    int foregroundFps() { return foregroundFps; }
    float objectDistance() { return objectDistance; }
    float objectDistanceSquared() { return objectDistance * objectDistance; }
    GraphicsPreset shifted(int direction) {
        GraphicsPreset[] values = values();
        return values[Math.floorMod(ordinal() + Integer.signum(direction), values.length)];
    }
}
