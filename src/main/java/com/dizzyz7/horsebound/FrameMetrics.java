// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class FrameMetrics {
    private static final int SAMPLE_COUNT = 120;
    private final float[] samples = new float[SAMPLE_COUNT];
    private int size;
    private int cursor;
    private float sum;

    void record(float deltaSeconds) {
        if (!Float.isFinite(deltaSeconds) || deltaSeconds <= 0f) return;
        float clamped = Math.min(deltaSeconds, 1f);
        if (size < samples.length) {
            samples[cursor] = clamped;
            sum += clamped;
            size++;
        } else {
            sum -= samples[cursor];
            samples[cursor] = clamped;
            sum += clamped;
        }
        cursor = (cursor + 1) % samples.length;
    }

    float averageMilliseconds() {
        return size == 0 ? 0f : (sum / size) * 1000f;
    }

    float worstMilliseconds() {
        float worst = 0f;
        for (int i = 0; i < size; i++) worst = Math.max(worst, samples[i]);
        return worst * 1000f;
    }

    int averageFps() {
        float averageSeconds = size == 0 ? 0f : sum / size;
        return averageSeconds <= 0f ? 0 : Math.round(1f / averageSeconds);
    }

    boolean meetsDeckTarget() {
        return size > 0 && averageMilliseconds() <= 33.34f;
    }
}
