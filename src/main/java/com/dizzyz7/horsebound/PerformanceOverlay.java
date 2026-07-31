// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import java.util.Locale;

final class PerformanceOverlay implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();

    void render(FrameMetrics metrics, GameSettings settings) {
        if (!settings.showPerformanceStats()) return;

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, settings.uiScale());
        String target = metrics.meetsDeckTarget() ? "PASS" : "CHECK";
        String line = String.format(
            Locale.ROOT,
            "FPS %d | avg %.1f ms | worst %.1f ms | 800p/30 %s | %s | %dx%d",
            metrics.averageFps(),
            metrics.averageMilliseconds(),
            metrics.worstMilliseconds(),
            target,
            settings.graphicsPreset().displayName(),
            width,
            height
        );

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.getData().setScale(0.78f * ui);
        font.setColor(metrics.meetsDeckTarget()
            ? new Color(0.68f, 1f, 0.72f, 1f)
            : new Color(1f, 0.72f, 0.48f, 1f));
        font.draw(batch, line, 18f * ui, 20f * ui);
        batch.end();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
