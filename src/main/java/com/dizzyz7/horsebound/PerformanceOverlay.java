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
    private final BitmapFont font = GameFonts.create();

    void render(FrameMetrics metrics, GameSettings settings) {
        if (!settings.showPerformanceStats()) return;

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, settings.uiScale());
        String target = I18n.text(metrics.meetsDeckTarget() ? "performance.pass" : "performance.check");
        String line = I18n.text(
            "performance.line",
            metrics.averageFps(),
            String.format(Locale.ROOT, "%.1f", metrics.averageMilliseconds()),
            String.format(Locale.ROOT, "%.1f", metrics.worstMilliseconds()),
            target,
            settings.graphicsPreset().displayName(),
            width,
            height
        );

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        GameFonts.setScale(font, 0.78f * ui);
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
