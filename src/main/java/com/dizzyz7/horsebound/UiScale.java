// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class UiScale {
    static final float BASE_WIDTH = 1280f;
    static final float BASE_HEIGHT = 800f;
    static final float MIN_EFFECTIVE_SCALE = 0.90f;
    static final float MAX_EFFECTIVE_SCALE = 2.00f;

    private UiScale() {
    }

    static float effective(int width, int height, float userScale) {
        float safeUserScale = Float.isFinite(userScale)
            ? Math.max(GameSettings.MIN_UI_SCALE, Math.min(GameSettings.MAX_UI_SCALE, userScale))
            : GameSettings.DEFAULT_UI_SCALE;
        float widthScale = Math.max(1, width) / BASE_WIDTH;
        float heightScale = Math.max(1, height) / BASE_HEIGHT;
        float screenScale = Math.min(widthScale, heightScale);
        screenScale = Math.max(MIN_EFFECTIVE_SCALE, Math.min(1.50f, screenScale));
        return Math.max(MIN_EFFECTIVE_SCALE, Math.min(MAX_EFFECTIVE_SCALE, screenScale * safeUserScale));
    }
}
