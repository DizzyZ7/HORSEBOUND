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
    private final BitmapFont font = new BitmapFont();
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
        float centerX = width * 0.5f;
        float startY = height * 0.55f;

        buttons[CONTINUE].set(centerX - 150f, startY + 78f, 300f, 54f);
        buttons[NEW_GAME].set(centerX - 150f, startY + 12f, 300f, 54f);
        buttons[LOAD_GAME].set(centerX - 150f, startY - 54f, 300f, 54f);
        buttons[SETTINGS].set(centerX - 150f, startY - 120f, 300f, 54f);
        buttons[EXIT].set(centerX - 150f, startY - 186f, 300f, 54f);

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
        font.getData().setScale(3.3f);
        font.draw(batch, "HORSEBOUND", centerX - 175f, height * 0.86f);

        font.getData().setScale(1.05f);
        font.setColor(new Color(0.80f, 0.90f, 0.82f, 1f));
        font.draw(batch, "A cozy 3D horse sandbox", centerX - 115f, height * 0.86f - 55f);

        drawButtonLabel("CONTINUE", CONTINUE, 92f, canContinue);
        drawButtonLabel("NEW GAME", NEW_GAME, 88f, true);
        drawButtonLabel("LOAD GAME", LOAD_GAME, 84f, true);
        drawButtonLabel("SETTINGS", SETTINGS, 94f, true);
        drawButtonLabel("EXIT", EXIT, 120f, true);

        font.getData().setScale(0.78f);
        font.setColor(new Color(0.66f, 0.74f, 0.68f, 1f));
        font.draw(batch,
            canContinue ? "Continue opens the most recently saved ranch." : "No ranch yet. Start a new world.",
            centerX - 165f,
            startY - 224f
        );
        font.draw(batch, inputHint(input.activeDevice()), centerX - 165f, startY - 247f);

        font.getData().setScale(0.9f);
        font.setColor(new Color(0.75f, 0.80f, 0.76f, 1f));
        font.draw(batch, "Created by Dimash Janibekov (DizZyZ7)", 22f, 38f);
        font.draw(batch, "(c) 2026 DizZyZ7. All rights reserved.", 22f, 20f);
        batch.end();
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
            if (buttons[i].contains(x, y)) {
                if (i == CONTINUE && !canContinue) return true;
                selectedIndex = i;
                activateSelected();
                return true;
            }
        }
        return false;
    }

    private Color buttonColor(int index) {
        if (index == CONTINUE && !canContinue) return new Color(0.12f, 0.15f, 0.14f, 1f);
        if (index == selectedIndex) return new Color(0.28f, 0.52f, 0.36f, 1f);
        if (index == EXIT) return new Color(0.16f, 0.20f, 0.18f, 1f);
        return new Color(0.17f, 0.31f, 0.23f, 1f);
    }

    private void drawButtonLabel(String label, int index, float offsetX, boolean enabled) {
        font.getData().setScale(1.16f);
        font.setColor(enabled ? Color.WHITE : new Color(0.48f, 0.52f, 0.49f, 1f));
        Rectangle button = buttons[index];
        font.draw(batch, label, button.x + offsetX, button.y + 35f);
    }

    private static String inputHint(InputDeviceType device) {
        return device == InputDeviceType.KEYBOARD_MOUSE
            ? "Arrows/WASD navigate | Enter confirm | Esc exit"
            : "D-pad/Left Stick navigate | A confirm | B back";
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
