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

final class KeyBindingsScreen implements Screen {
    private static final BindableAction[] ACTIONS = BindableAction.values();
    private static final int DEFAULTS_INDEX = ACTIONS.length;
    private static final int BACK_INDEX = ACTIONS.length + 1;
    private static final int ITEM_COUNT = ACTIONS.length + 2;

    private final HorseboundGame game;
    private final LivingRanchScreen pausedWorld;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final MenuInputMapper menuInput = new MenuInputMapper();
    private final Rectangle[] rows = new Rectangle[ITEM_COUNT];
    private InputProfile profile;
    private int selectedIndex;
    private BindableAction capturing;
    private String message = "Select an action, confirm, then press a keyboard key.";

    KeyBindingsScreen(HorseboundGame game, LivingRanchScreen pausedWorld) {
        this.game = game;
        this.pausedWorld = pausedWorld;
        this.profile = game.inputProfile();
        for (int i = 0; i < rows.length; i++) rows[i] = new Rectangle();
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        profile = game.inputProfile();
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, game.settings().uiScale());
        float geometry = Math.min(ui, 1.0f);
        float rowWidth = Math.min(width - 34f * geometry, 720f * geometry);
        float rowHeight = 31f * geometry;
        float gap = 35f * geometry;
        float x = (width - rowWidth) * 0.5f;
        float startY = height - 84f * geometry;
        for (int i = 0; i < rows.length; i++) rows[i].set(x, startY - i * gap, rowWidth, rowHeight);

        MenuInputSnapshot input = menuInput.sample();
        if (capturing != null) {
            if (input.command().backPressed()) {
                capturing = null;
                message = "Binding cancelled. Select another action or go back.";
            } else {
                captureKey();
            }
        } else {
            if (input.command().upPressed()) selectedIndex = Math.floorMod(selectedIndex - 1, ITEM_COUNT);
            if (input.command().downPressed()) selectedIndex = Math.floorMod(selectedIndex + 1, ITEM_COUNT);
            if (input.command().confirmPressed() && activate()) return;
            if (input.command().backPressed()) {
                goBack();
                return;
            }
            if (handlePointer(height)) return;
        }

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
        font.getData().setScale(1.75f * ui);
        font.draw(batch, "KEYBOARD BINDINGS", width * 0.5f - 170f * ui, height - 24f * geometry);
        font.getData().setScale(0.78f * ui);
        for (int i = 0; i < ACTIONS.length; i++) {
            BindableAction action = ACTIONS[i];
            Rectangle row = rows[i];
            font.setColor(i == selectedIndex ? Color.WHITE : new Color(0.84f, 0.90f, 0.85f, 1f));
            font.draw(batch, action.displayName(), row.x + 12f * ui, row.y + 22f * ui);
            font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
            font.draw(batch, KeyLabel.of(profile.keyFor(action)), row.x + row.width - 155f * ui, row.y + 22f * ui);
        }
        drawFooterRow(DEFAULTS_INDEX, "RESTORE DEFAULT BINDINGS", ui);
        drawFooterRow(BACK_INDEX, "BACK", ui);
        font.getData().setScale(0.66f * ui);
        font.setColor(capturing == null
            ? new Color(0.70f, 0.78f, 0.71f, 1f)
            : new Color(1f, 0.86f, 0.48f, 1f));
        font.draw(batch, message, 18f * geometry, 48f * geometry);
        batch.end();
    }

    private void captureKey() {
        for (int keyCode = 1; keyCode <= 255; keyCode++) {
            if (!Gdx.input.isKeyJustPressed(keyCode)) continue;
            profile = profile.withBinding(capturing, keyCode);
            game.updateInputProfile(profile);
            message = capturing.displayName() + " is now bound to " + KeyLabel.of(keyCode) + ".";
            capturing = null;
            return;
        }
    }

    private boolean activate() {
        if (selectedIndex < ACTIONS.length) {
            capturing = ACTIONS[selectedIndex];
            message = "Press a keyboard key for " + capturing.displayName() + ". Esc / Back cancels.";
            return false;
        }
        if (selectedIndex == DEFAULTS_INDEX) {
            profile = InputProfile.defaults();
            game.updateInputProfile(profile);
            message = "Default keyboard bindings restored.";
            return false;
        }
        goBack();
        return true;
    }

    private void drawFooterRow(int index, String text, float ui) {
        Rectangle row = rows[index];
        font.setColor(index == selectedIndex ? Color.WHITE : new Color(0.84f, 0.90f, 0.85f, 1f));
        font.draw(batch, text, row.x + 12f * ui, row.y + 22f * ui);
    }

    private boolean handlePointer(int height) {
        if (!Gdx.input.justTouched()) return false;
        menuInput.markPointerActive();
        float x = Gdx.input.getX();
        float y = height - Gdx.input.getY();
        for (int i = 0; i < rows.length; i++) {
            if (!rows[i].contains(x, y)) continue;
            selectedIndex = i;
            return activate();
        }
        return false;
    }

    private void goBack() {
        game.showInputSettings(pausedWorld);
    }

    @Override public void resize(int width, int height) { }

    @Override
    public void pause() {
        if (pausedWorld != null) pausedWorld.pause();
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
