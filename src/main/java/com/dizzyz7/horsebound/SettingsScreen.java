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

import java.util.Locale;

final class SettingsScreen implements Screen {
    private static final int VSYNC = 0;
    private static final int SENSITIVITY = 1;
    private static final int AUTOSAVE = 2;
    private static final int DEFAULTS = 3;
    private static final int BACK = 4;
    private static final int ITEM_COUNT = 5;

    private final HorseboundGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final MenuInputMapper menuInput = new MenuInputMapper();

    private final Rectangle vsyncButton = new Rectangle();
    private final Rectangle sensitivityMinus = new Rectangle();
    private final Rectangle sensitivityPlus = new Rectangle();
    private final Rectangle autosaveMinus = new Rectangle();
    private final Rectangle autosavePlus = new Rectangle();
    private final Rectangle defaultsButton = new Rectangle();
    private final Rectangle backButton = new Rectangle();

    private GameSettings settings;
    private int selectedIndex;

    SettingsScreen(HorseboundGame game) {
        this.game = game;
        this.settings = game.settings();
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        selectedIndex = VSYNC;
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float centerX = width * 0.5f;
        float top = height * 0.64f;

        vsyncButton.set(centerX + 65f, top + 38f, 180f, 48f);
        sensitivityMinus.set(centerX + 65f, top - 34f, 54f, 48f);
        sensitivityPlus.set(centerX + 191f, top - 34f, 54f, 48f);
        autosaveMinus.set(centerX + 65f, top - 106f, 54f, 48f);
        autosavePlus.set(centerX + 191f, top - 106f, 54f, 48f);
        defaultsButton.set(centerX - 210f, top - 205f, 190f, 52f);
        backButton.set(centerX + 20f, top - 205f, 190f, 52f);

        MenuInputSnapshot input = menuInput.sample();
        if (handleNavigation(input.command())) return;
        if (handlePointer(height)) return;

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.10f, 0.16f, 0.14f, 1f));
        shapes.rect(0f, 0f, width, height);

        shapes.setColor(selectedIndex == VSYNC
            ? selectedColor()
            : settings.vsync() ? new Color(0.18f, 0.36f, 0.25f, 1f) : new Color(0.25f, 0.22f, 0.18f, 1f));
        shapes.rect(vsyncButton.x, vsyncButton.y, vsyncButton.width, vsyncButton.height);

        shapes.setColor(selectedIndex == SENSITIVITY ? selectedColor() : normalColor());
        shapes.rect(sensitivityMinus.x, sensitivityMinus.y, sensitivityMinus.width, sensitivityMinus.height);
        shapes.rect(sensitivityPlus.x, sensitivityPlus.y, sensitivityPlus.width, sensitivityPlus.height);

        shapes.setColor(selectedIndex == AUTOSAVE ? selectedColor() : normalColor());
        shapes.rect(autosaveMinus.x, autosaveMinus.y, autosaveMinus.width, autosaveMinus.height);
        shapes.rect(autosavePlus.x, autosavePlus.y, autosavePlus.width, autosavePlus.height);

        shapes.setColor(selectedIndex == DEFAULTS ? selectedColor() : normalColor());
        shapes.rect(defaultsButton.x, defaultsButton.y, defaultsButton.width, defaultsButton.height);
        shapes.setColor(selectedIndex == BACK ? selectedColor() : normalColor());
        shapes.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(2.4f);
        font.draw(batch, "SETTINGS", centerX - 120f, height * 0.82f);

        font.getData().setScale(1.05f);
        font.setColor(new Color(0.84f, 0.90f, 0.85f, 1f));
        font.draw(batch, "VSync", centerX - 220f, top + 72f);
        font.draw(batch, "Mouse / camera sensitivity", centerX - 220f, top);
        font.draw(batch, "Autosave interval", centerX - 220f, top - 72f);

        font.setColor(Color.WHITE);
        font.draw(batch, settings.vsync() ? "ON" : "OFF", vsyncButton.x + 72f, vsyncButton.y + 32f);
        font.draw(batch, "-", sensitivityMinus.x + 22f, sensitivityMinus.y + 32f);
        font.draw(batch, "+", sensitivityPlus.x + 20f, sensitivityPlus.y + 32f);
        font.draw(batch, "-", autosaveMinus.x + 22f, autosaveMinus.y + 32f);
        font.draw(batch, "+", autosavePlus.x + 20f, autosavePlus.y + 32f);

        font.getData().setScale(0.95f);
        font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
        font.draw(batch, String.format(Locale.ROOT, "%.2f", settings.mouseSensitivity()), centerX + 137f, top - 2f);
        font.draw(batch, settings.autosaveSeconds() + " sec", centerX + 128f, top - 74f);

        font.setColor(Color.WHITE);
        font.draw(batch, "DEFAULTS", defaultsButton.x + 46f, defaultsButton.y + 34f);
        font.draw(batch, "BACK", backButton.x + 72f, backButton.y + 34f);

        font.getData().setScale(0.72f);
        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.draw(batch, "Settings are stored separately from ranch saves.", centerX - 155f, 58f);
        font.draw(batch, inputHint(input.activeDevice()), centerX - 205f, 39f);
        font.draw(batch, "Created by Dimash Janibekov (DizZyZ7)", 18f, 20f);
        batch.end();
    }

    private boolean handleNavigation(MenuCommand command) {
        if (command.upPressed()) selectedIndex = Math.floorMod(selectedIndex - 1, ITEM_COUNT);
        if (command.downPressed()) selectedIndex = Math.floorMod(selectedIndex + 1, ITEM_COUNT);
        if (command.leftPressed()) adjustSelected(-1);
        if (command.rightPressed()) adjustSelected(1);
        if (command.confirmPressed()) {
            switch (selectedIndex) {
                case VSYNC -> apply(settings.withVsync(!settings.vsync()));
                case SENSITIVITY -> adjustSelected(1);
                case AUTOSAVE -> adjustSelected(1);
                case DEFAULTS -> apply(GameSettings.defaults());
                case BACK -> {
                    game.returnToMenu();
                    return true;
                }
                default -> throw new IllegalStateException("Unknown settings index: " + selectedIndex);
            }
        }
        if (command.backPressed()) {
            game.returnToMenu();
            return true;
        }
        return false;
    }

    private void adjustSelected(int direction) {
        switch (selectedIndex) {
            case VSYNC -> {
                if (direction != 0) apply(settings.withVsync(!settings.vsync()));
            }
            case SENSITIVITY -> apply(settings.withMouseSensitivity(settings.mouseSensitivity() + 0.01f * direction));
            case AUTOSAVE -> apply(settings.withAutosaveSeconds(settings.autosaveSeconds() + 30 * direction));
            default -> { }
        }
    }

    private boolean handlePointer(int height) {
        if (!Gdx.input.justTouched()) return false;
        menuInput.markPointerActive();
        float x = Gdx.input.getX();
        float y = height - Gdx.input.getY();
        if (vsyncButton.contains(x, y)) {
            selectedIndex = VSYNC;
            apply(settings.withVsync(!settings.vsync()));
        } else if (sensitivityMinus.contains(x, y)) {
            selectedIndex = SENSITIVITY;
            adjustSelected(-1);
        } else if (sensitivityPlus.contains(x, y)) {
            selectedIndex = SENSITIVITY;
            adjustSelected(1);
        } else if (autosaveMinus.contains(x, y)) {
            selectedIndex = AUTOSAVE;
            adjustSelected(-1);
        } else if (autosavePlus.contains(x, y)) {
            selectedIndex = AUTOSAVE;
            adjustSelected(1);
        } else if (defaultsButton.contains(x, y)) {
            selectedIndex = DEFAULTS;
            apply(GameSettings.defaults());
        } else if (backButton.contains(x, y)) {
            selectedIndex = BACK;
            game.returnToMenu();
            return true;
        }
        return false;
    }

    private void apply(GameSettings next) {
        settings = next;
        game.updateSettings(next);
    }

    private static Color selectedColor() {
        return new Color(0.28f, 0.52f, 0.36f, 1f);
    }

    private static Color normalColor() {
        return new Color(0.16f, 0.23f, 0.19f, 1f);
    }

    private static String inputHint(InputDeviceType device) {
        return device == InputDeviceType.KEYBOARD_MOUSE
            ? "Up/Down select | Left/Right change | Enter confirm | Esc back"
            : "D-pad/Left Stick select | Left/Right change | A confirm | B back";
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
