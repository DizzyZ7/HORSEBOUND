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
    private static final int WINDOW_MODE = 0;
    private static final int RESOLUTION = 1;
    private static final int VSYNC = 2;
    private static final int GRAPHICS = 3;
    private static final int UI_SCALE = 4;
    private static final int SENSITIVITY = 5;
    private static final int PERFORMANCE = 6;
    private static final int AUTOSAVE = 7;
    private static final int DEFAULTS = 8;
    private static final int BACK = 9;
    private static final int ITEM_COUNT = 10;

    private final HorseboundGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final MenuInputMapper menuInput = new MenuInputMapper();
    private final Rectangle[] rows = new Rectangle[ITEM_COUNT];

    private GameSettings settings;
    private int selectedIndex;

    SettingsScreen(HorseboundGame game) {
        this.game = game;
        this.settings = game.settings();
        for (int i = 0; i < rows.length; i++) rows[i] = new Rectangle();
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        selectedIndex = WINDOW_MODE;
        settings = game.settings();
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, settings.uiScale());
        layout(width, height, ui);

        MenuInputSnapshot input = menuInput.sample();
        if (handleNavigation(input.command())) return;
        if (handlePointer(height)) return;

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.10f, 0.16f, 0.14f, 1f));
        shapes.rect(0f, 0f, width, height);
        for (int i = 0; i < rows.length; i++) {
            shapes.setColor(i == selectedIndex ? selectedColor() : normalColor());
            Rectangle row = rows[i];
            shapes.rect(row.x, row.y, row.width, row.height);
        }
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(2.25f * ui);
        font.draw(batch, "SETTINGS", width * 0.5f - 105f * ui, height - 34f * ui);

        font.getData().setScale(0.92f * ui);
        for (int i = 0; i < rows.length; i++) drawRow(i, rows[i], ui);

        font.getData().setScale(0.72f * ui);
        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.draw(batch, "Display settings are device-local and excluded from Steam Cloud.", 18f * ui, 42f * ui);
        font.draw(batch, InputPromptCatalog.menuHint(input.activeDevice()), 18f * ui, 23f * ui);
        batch.end();
    }

    private void layout(int width, int height, float ui) {
        float rowWidth = Math.min(width - 40f * ui, 680f * ui);
        float rowHeight = 40f * ui;
        float gap = 46f * ui;
        float startY = height - 116f * ui;
        float x = (width - rowWidth) * 0.5f;
        for (int i = 0; i < rows.length; i++) {
            rows[i].set(x, startY - i * gap, rowWidth, rowHeight);
        }
    }

    private void drawRow(int index, Rectangle row, float ui) {
        font.setColor(index == selectedIndex ? Color.WHITE : new Color(0.84f, 0.90f, 0.85f, 1f));
        font.draw(batch, label(index), row.x + 14f * ui, row.y + 27f * ui);
        String value = value(index);
        if (!value.isEmpty()) {
            font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
            font.draw(batch, value, row.x + row.width - 245f * ui, row.y + 27f * ui);
        }
    }

    private String label(int index) {
        return switch (index) {
            case WINDOW_MODE -> "Window mode";
            case RESOLUTION -> "Windowed resolution";
            case VSYNC -> "VSync";
            case GRAPHICS -> "Graphics preset";
            case UI_SCALE -> "UI / text scale";
            case SENSITIVITY -> "Mouse / camera sensitivity";
            case PERFORMANCE -> "Performance overlay";
            case AUTOSAVE -> "Autosave interval";
            case DEFAULTS -> "Restore Deck-safe defaults";
            case BACK -> "Back";
            default -> throw new IllegalArgumentException("Unknown settings index: " + index);
        };
    }

    private String value(int index) {
        return switch (index) {
            case WINDOW_MODE -> settings.windowMode().displayName();
            case RESOLUTION -> settings.displayResolution().displayName();
            case VSYNC -> settings.vsync() ? "ON" : "OFF";
            case GRAPHICS -> settings.graphicsPreset().displayName();
            case UI_SCALE -> Math.round(settings.uiScale() * 100f) + "%";
            case SENSITIVITY -> String.format(Locale.ROOT, "%.2f", settings.mouseSensitivity());
            case PERFORMANCE -> settings.showPerformanceStats() ? "ON" : "OFF";
            case AUTOSAVE -> settings.autosaveSeconds() + " sec";
            case DEFAULTS, BACK -> "";
            default -> throw new IllegalArgumentException("Unknown settings index: " + index);
        };
    }

    private boolean handleNavigation(MenuCommand command) {
        if (command.upPressed()) selectedIndex = Math.floorMod(selectedIndex - 1, ITEM_COUNT);
        if (command.downPressed()) selectedIndex = Math.floorMod(selectedIndex + 1, ITEM_COUNT);
        if (command.leftPressed()) adjustSelected(-1);
        if (command.rightPressed()) adjustSelected(1);
        if (command.confirmPressed()) {
            if (selectedIndex == DEFAULTS) {
                apply(GameSettings.defaults());
            } else if (selectedIndex == BACK) {
                game.returnToMenu();
                return true;
            } else {
                adjustSelected(1);
            }
        }
        if (command.backPressed()) {
            game.returnToMenu();
            return true;
        }
        return false;
    }

    private void adjustSelected(int direction) {
        if (direction == 0) return;
        switch (selectedIndex) {
            case WINDOW_MODE -> apply(settings.withWindowMode(settings.windowMode().toggled()));
            case RESOLUTION -> apply(settings.withResolution(settings.displayResolution().shifted(direction)));
            case VSYNC -> apply(settings.withVsync(!settings.vsync()));
            case GRAPHICS -> apply(settings.withGraphicsPreset(settings.graphicsPreset().shifted(direction)));
            case UI_SCALE -> apply(settings.withUiScale(settings.uiScale() + 0.10f * Integer.signum(direction)));
            case SENSITIVITY -> apply(settings.withMouseSensitivity(settings.mouseSensitivity() + 0.01f * direction));
            case PERFORMANCE -> apply(settings.withPerformanceStats(!settings.showPerformanceStats()));
            case AUTOSAVE -> apply(settings.withAutosaveSeconds(settings.autosaveSeconds() + 30 * direction));
            default -> { }
        }
    }

    private boolean handlePointer(int height) {
        if (!Gdx.input.justTouched()) return false;
        menuInput.markPointerActive();
        float x = Gdx.input.getX();
        float y = height - Gdx.input.getY();
        for (int i = 0; i < rows.length; i++) {
            Rectangle row = rows[i];
            if (!row.contains(x, y)) continue;
            selectedIndex = i;
            if (i == DEFAULTS) {
                apply(GameSettings.defaults());
            } else if (i == BACK) {
                game.returnToMenu();
                return true;
            } else {
                adjustSelected(x < row.x + row.width * 0.5f ? -1 : 1);
            }
            break;
        }
        return false;
    }

    private void apply(GameSettings next) {
        game.updateSettings(next);
        settings = game.settings();
    }

    private static Color selectedColor() {
        return new Color(0.28f, 0.52f, 0.36f, 1f);
    }

    private static Color normalColor() {
        return new Color(0.16f, 0.23f, 0.19f, 1f);
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
