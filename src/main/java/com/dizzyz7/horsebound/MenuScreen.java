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

final class MenuScreen implements Screen {
    private static final int CONTINUE = 0;
    private static final int NEW_GAME = 1;
    private static final int LOAD_GAME = 2;
    private static final int SETTINGS = 3;
    private static final int EXIT = 4;
    private static final int ITEM_COUNT = 5;

    private final HorseboundGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = GameFonts.create();
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final MenuInputMapper menuInput = new MenuInputMapper();
    private final Rectangle[] buttons = {
        new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle(), new Rectangle()
    };

    private boolean canContinue;
    private int selectedIndex;

    MenuScreen(HorseboundGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        canContinue = game.hasContinue();
        selectedIndex = canContinue ? CONTINUE : NEW_GAME;
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, game.settings().uiScale());
        float layoutScale = Math.min(ui, 1.25f);
        layout(width, height, layoutScale);

        MenuInputSnapshot input = menuInput.sample();
        if (handleNavigation(input.command())) return;
        if (handlePointer(height)) return;

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.12f, 0.18f, 0.16f, 1f));
        shapes.rect(0f, 0f, width, height);
        for (int i = 0; i < ITEM_COUNT; i++) {
            shapes.setColor(buttonColor(i));
            Rectangle button = buttons[i];
            shapes.rect(button.x, button.y, button.width, button.height);
        }
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        GameFonts.setScale(font, 3.1f * ui);
        drawCentered("HORSEBOUND", width * 0.5f, height * 0.88f);

        GameFonts.setScale(font, 1.02f * ui);
        font.setColor(new Color(0.80f, 0.90f, 0.82f, 1f));
        drawCentered(I18n.text("menu.tagline"), width * 0.5f, height * 0.80f);

        drawButtonLabel(I18n.text("menu.continue"), CONTINUE, canContinue, ui);
        drawButtonLabel(I18n.text("menu.new_game"), NEW_GAME, true, ui);
        drawButtonLabel(I18n.text("menu.load_game"), LOAD_GAME, true, ui);
        drawButtonLabel(I18n.text("menu.settings"), SETTINGS, true, ui);
        drawButtonLabel(I18n.text("menu.exit"), EXIT, true, ui);

        GameFonts.setScale(font, 0.78f * ui);
        font.setColor(new Color(0.66f, 0.74f, 0.68f, 1f));
        drawCentered(
            canContinue ? I18n.text("menu.continue_hint") : I18n.text("menu.new_hint"),
            width * 0.5f,
            91f * layoutScale
        );
        drawCentered(InputPromptCatalog.menuHint(input.activeDevice()), width * 0.5f, 67f * layoutScale);

        GameFonts.setScale(font, 0.78f * ui);
        font.setColor(new Color(0.75f, 0.80f, 0.76f, 1f));
        font.draw(batch, I18n.text("common.created_by"), 18f * layoutScale, 38f * layoutScale);
        batch.end();
    }

    private void layout(int width, int height, float scale) {
        float buttonWidth = 320f * scale;
        float buttonHeight = 52f * scale;
        float gap = 62f * scale;
        float x = (width - buttonWidth) * 0.5f;
        float firstY = height * 0.61f;
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].set(x, firstY - i * gap, buttonWidth, buttonHeight);
        }
    }

    private boolean handleNavigation(MenuCommand command) {
        if (command.upPressed()) selectedIndex = nextEnabled(selectedIndex, -1);
        if (command.downPressed()) selectedIndex = nextEnabled(selectedIndex, 1);
        if (command.confirmPressed()) {
            activateSelected();
            return true;
        }
        if (command.backPressed()) {
            Gdx.app.exit();
            return true;
        }
        return false;
    }

    private int nextEnabled(int from, int direction) {
        int next = from;
        do {
            next = Math.floorMod(next + direction, ITEM_COUNT);
        } while (next == CONTINUE && !canContinue);
        return next;
    }

    private void activateSelected() {
        switch (selectedIndex) {
            case CONTINUE -> {
                if (canContinue) game.continueWorld();
            }
            case NEW_GAME -> game.showNewGameSlots();
            case LOAD_GAME -> game.showLoadGameSlots();
            case SETTINGS -> game.showSettings();
            case EXIT -> Gdx.app.exit();
            default -> throw new IllegalStateException("Unknown menu index: " + selectedIndex);
        }
    }

    private boolean handlePointer(int height) {
        if (!Gdx.input.justTouched()) return false;
        menuInput.markPointerActive();
        float x = Gdx.input.getX();
        float y = height - Gdx.input.getY();
        for (int i = 0; i < ITEM_COUNT; i++) {
            if (!buttons[i].contains(x, y)) continue;
            if (i == CONTINUE && !canContinue) return true;
            selectedIndex = i;
            activateSelected();
            return true;
        }
        return false;
    }

    private Color buttonColor(int index) {
        if (index == CONTINUE && !canContinue) return new Color(0.12f, 0.15f, 0.14f, 1f);
        if (index == selectedIndex) return new Color(0.28f, 0.52f, 0.36f, 1f);
        if (index == EXIT) return new Color(0.16f, 0.20f, 0.18f, 1f);
        return new Color(0.17f, 0.31f, 0.23f, 1f);
    }

    private void drawButtonLabel(String label, int index, boolean enabled, float ui) {
        GameFonts.setScale(font, 1.08f * ui);
        font.setColor(enabled ? Color.WHITE : new Color(0.48f, 0.52f, 0.49f, 1f));
        Rectangle button = buttons[index];
        glyphLayout.setText(font, label);
        font.draw(
            batch,
            label,
            button.x + (button.width - glyphLayout.width) * 0.5f,
            button.y + (button.height + glyphLayout.height) * 0.5f
        );
    }

    private void drawCentered(String text, float centerX, float baselineY) {
        glyphLayout.setText(font, text);
        font.draw(batch, text, centerX - glyphLayout.width * 0.5f, baselineY);
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
