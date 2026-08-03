// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

/** Pauses simulation without disposing or recreating the active ranch. */
final class PauseScreen implements Screen {
    private static final int RESUME = 0;
    private static final int UNDO = 1;
    private static final int INPUT = 2;
    private static final int SAVE = 3;
    private static final int MAIN_MENU = 4;
    private static final int ITEM_COUNT = 5;

    private final HorseboundGame game;
    private final RanchSessionScreen world;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = GameFonts.create();
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final MenuInputMapper menuInput = new MenuInputMapper();
    private final Rectangle[] rows = {
        new Rectangle(),
        new Rectangle(),
        new Rectangle(),
        new Rectangle(),
        new Rectangle()
    };
    private int selectedIndex;
    private String message = I18n.text("pause.message");

    PauseScreen(HorseboundGame game, RanchSessionScreen world) {
        this.game = game;
        this.world = world;
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
        float rowWidth = Math.min(width - 40f * geometry, 520f * geometry);
        float rowHeight = 48f * geometry;
        float x = (width - rowWidth) * 0.5f;
        float startY = height * 0.60f;
        for (int i = 0; i < rows.length; i++) rows[i].set(x, startY - i * 60f * geometry, rowWidth, rowHeight);

        MenuInputSnapshot input = menuInput.sample();
        if (input.command().upPressed()) selectedIndex = Math.floorMod(selectedIndex - 1, ITEM_COUNT);
        if (input.command().downPressed()) selectedIndex = Math.floorMod(selectedIndex + 1, ITEM_COUNT);
        if (input.command().confirmPressed()) {
            activate();
            return;
        }
        if (input.command().backPressed()) {
            game.resumePausedWorld(world);
            return;
        }
        if (handlePointer(height)) return;

        Gdx.gl.glClearColor(0.035f, 0.055f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.08f, 0.13f, 0.11f, 1f));
        shapes.rect(0f, 0f, width, height);
        for (int i = 0; i < rows.length; i++) {
            shapes.setColor(i == selectedIndex
                ? new Color(0.28f, 0.52f, 0.36f, 1f)
                : new Color(0.16f, 0.24f, 0.19f, 1f));
            Rectangle row = rows[i];
            shapes.rect(row.x, row.y, row.width, row.height);
        }
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        GameFonts.setScale(font, 2.4f * ui);
        drawCentered(I18n.text("pause.title"), width * 0.5f, height * 0.84f);
        GameFonts.setScale(font, 0.98f * ui);
        drawLabel(I18n.text("pause.resume"), rows[RESUME], ui);
        drawLabel(
            world.hasUndoableRanchEdit() ? I18n.text("pause.undo") : I18n.text("pause.undo_unavailable"),
            rows[UNDO],
            ui,
            world.hasUndoableRanchEdit()
        );
        drawLabel(I18n.text("pause.input"), rows[INPUT], ui);
        drawLabel(I18n.text("pause.save"), rows[SAVE], ui);
        drawLabel(I18n.text("pause.save_menu"), rows[MAIN_MENU], ui);
        GameFonts.setScale(font, 0.76f * ui);
        font.setColor(new Color(0.78f, 0.86f, 0.79f, 1f));
        font.draw(batch, message, x, startY - 318f * geometry);
        batch.end();
    }

    private void drawCentered(String value, float centerX, float baselineY) {
        glyphLayout.setText(font, value);
        font.draw(batch, value, centerX - glyphLayout.width * 0.5f, baselineY);
    }

    private void drawLabel(String text, Rectangle row, float ui) {
        drawLabel(text, row, ui, true);
    }

    private void drawLabel(String text, Rectangle row, float ui, boolean enabled) {
        font.setColor(enabled ? Color.WHITE : new Color(0.48f, 0.54f, 0.49f, 1f));
        font.draw(batch, text, row.x + 18f * ui, row.y + 32f * ui);
    }

    private void activate() {
        switch (selectedIndex) {
            case RESUME -> game.resumePausedWorld(world);
            case UNDO -> {
                if (!world.hasUndoableRanchEdit()) {
                    message = I18n.text("pause.no_undo");
                    return;
                }
                message = world.undoLastRanchEdit();
                ControllerRumble.pulse(game.inputProfile(), 45, 0.34f);
            }
            case INPUT -> game.showInputSettings(world);
            case SAVE -> {
                world.saveSession();
                ControllerRumble.pulse(game.inputProfile(), 55, 0.45f);
                message = I18n.text("pause.saved");
            }
            case MAIN_MENU -> game.leavePausedWorldToMenu(world);
            default -> throw new IllegalStateException("Unknown pause item: " + selectedIndex);
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

    @Override
    public void pause() {
        world.saveSession();
    }

    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}
