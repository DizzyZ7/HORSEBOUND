// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

final class SettingsHubScreen implements Screen {
    private static final int DISPLAY = 0;
    private static final int INPUT = 1;
    private static final int BACK = 2;
    private static final int ITEM_COUNT = 3;

    private final HorseboundGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final MenuInputMapper menuInput = new MenuInputMapper();
    private final Rectangle[] rows = {new Rectangle(), new Rectangle(), new Rectangle()};
    private int selectedIndex;

    SettingsHubScreen(HorseboundGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, game.settings().uiScale());
        float geometry = Math.min(ui, 1.20f);
        float rowWidth = Math.min(width - 40f * geometry, 560f * geometry);
        float rowHeight = 58f * geometry;
        float x = (width - rowWidth) * 0.5f;
        float top = height * 0.58f;
        for (int i = 0; i < rows.length; i++) rows[i].set(x, top - i * 76f * geometry, rowWidth, rowHeight);

        MenuInputSnapshot input = menuInput.sample();
        if (input.command().upPressed()) selectedIndex = Math.floorMod(selectedIndex - 1, ITEM_COUNT);
        if (input.command().downPressed()) selectedIndex = Math.floorMod(selectedIndex + 1, ITEM_COUNT);
        if (input.command().confirmPressed()) {
            activate();
            return;
        }
        if (input.command().backPressed()) {
            game.returnToMenu();
            return;
        }
        if (handlePointer(height)) return;

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.10f, 0.16f, 0.14f, 1f));
        shapes.rect(0f, 0f, width, height);
        for (int i = 0; i < rows.length; i++) {
            shapes.setColor(i == selectedIndex
                ? new Color(0.28f, 0.52f, 0.36f, 1f)
                : new Color(0.16f, 0.23f, 0.19f, 1f));
            Rectangle row = rows[i];
            shapes.rect(row.x, row.y, row.width, row.height);
        }
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(2.35f * ui);
        font.draw(batch, "SETTINGS", width * 0.5f - 115f * ui, height * 0.82f);
        font.getData().setScale(1.08f * ui);
        draw("DISPLAY & GRAPHICS", rows[DISPLAY], ui);
        draw("INPUT & ACCESSIBILITY", rows[INPUT], ui);
        draw("BACK", rows[BACK], ui);
        font.getData().setScale(0.70f * ui);
        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.draw(batch, "Device settings stay outside ranch saves and Steam Cloud.", x, top - 250f * geometry);
        batch.end();
    }

    private void draw(String text, Rectangle row, float ui) {
        font.setColor(Color.WHITE);
        font.draw(batch, text, row.x + 18f * ui, row.y + 38f * ui);
    }

    private void activate() {
        switch (selectedIndex) {
            case DISPLAY -> game.showDisplaySettings();
            case INPUT -> game.showInputSettings(null);
            case BACK -> game.returnToMenu();
            default -> throw new IllegalStateException("Unknown settings hub item: " + selectedIndex);
        }
    }

    private boolean handlePointer(int height) {
        if (!Gdx.input.justTouched()) return false;
        menuInput.markPointerActive();
        float x = Gdx.input.getX();
        float y = height - Gdx.input.getY();
        for (int i = 0; i < rows.length; i++) {
            if (!rows[i].contains(x, y)) continue;
            selectedIndex = i;
            activate();
            return true;
        }
        return false;
    }

    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}
