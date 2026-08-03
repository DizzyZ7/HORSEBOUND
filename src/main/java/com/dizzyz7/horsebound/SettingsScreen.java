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

import java.util.Locale;

final class SettingsScreen implements Screen {
    private static final int WINDOW_MODE = 0;
    private static final int RESOLUTION = 1;
    private static final int VSYNC = 2;
    private static final int GRAPHICS = 3;
    private static final int UI_SCALE = 4;
    private static final int SENSITIVITY = 5;
    private static final int SFX_VOLUME = 6;
    private static final int AMBIENCE_VOLUME = 7;
    private static final int PERFORMANCE = 8;
    private static final int AUTOSAVE = 9;
    private static final int DEFAULTS = 10;
    private static final int BACK = 11;
    private static final int ITEM_COUNT = 12;

    private final HorseboundGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = GameFonts.create();
    private final GlyphLayout glyphLayout = new GlyphLayout();
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
        float layoutScale = Math.min(ui, 1.15f);
        layout(width, height, layoutScale);

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
        GameFonts.setScale(font, 2.25f * ui);
        drawCentered(I18n.text("settings.title"), width * 0.5f, height - 34f * layoutScale);

        GameFonts.setScale(font, 0.92f * ui);
        for (int i = 0; i < rows.length; i++) drawRow(i, rows[i], ui, layoutScale);

        GameFonts.setScale(font, 0.72f * ui);
        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.draw(batch, I18n.text("settings.display_notice"), 18f * layoutScale, 42f * layoutScale);
        font.draw(batch, InputPromptCatalog.menuHint(input.activeDevice()), 18f * layoutScale, 23f * layoutScale);
        batch.end();
    }

    private void layout(int width, int height, float scale) {
        float rowWidth = Math.min(width - 40f * scale, 680f * scale);
        float rowHeight = 34f * scale;
        float gap = 38f * scale;
        float startY = height - 92f * scale;
        float x = (width - rowWidth) * 0.5f;
        for (int i = 0; i < rows.length; i++) {
            rows[i].set(x, startY - i * gap, rowWidth, rowHeight);
        }
    }

    private void drawRow(int index, Rectangle row, float ui, float layoutScale) {
        font.setColor(index == selectedIndex ? Color.WHITE : new Color(0.84f, 0.90f, 0.85f, 1f));
        font.draw(batch, label(index), row.x + 14f * layoutScale, row.y + 25f * layoutScale);
        String value = value(index);
        if (!value.isEmpty()) {
            font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
            glyphLayout.setText(font, value);
            font.draw(batch, value, row.x + row.width - 14f * layoutScale - glyphLayout.width, row.y + 25f * layoutScale);
        }
    }

    private String label(int index) {
        return switch (index) {
            case WINDOW_MODE -> I18n.text("settings.window_mode");
            case RESOLUTION -> I18n.text("settings.resolution");
            case VSYNC -> I18n.text("settings.vsync");
            case GRAPHICS -> I18n.text("settings.graphics");
            case UI_SCALE -> I18n.text("settings.ui_scale");
            case SENSITIVITY -> I18n.text("settings.sensitivity");
            case SFX_VOLUME -> I18n.text("settings.sfx");
            case AMBIENCE_VOLUME -> I18n.text("settings.ambience");
            case PERFORMANCE -> I18n.text("settings.performance");
            case AUTOSAVE -> I18n.text("settings.autosave");
            case DEFAULTS -> I18n.text("settings.restore_defaults");
            case BACK -> I18n.text("settings.back");
            default -> throw new IllegalArgumentException("Unknown settings index: " + index);
        };
    }

    private String value(int index) {
        return switch (index) {
            case WINDOW_MODE -> settings.windowMode().displayName();
            case RESOLUTION -> settings.displayResolution().displayName();
            case VSYNC -> settings.vsync() ? I18n.text("common.on") : I18n.text("common.off");
            case GRAPHICS -> I18n.text("settings.msaa_restart", settings.graphicsPreset().displayName());
            case UI_SCALE -> Math.round(settings.uiScale() * 100f) + "%";
            case SENSITIVITY -> String.format(Locale.ROOT, "%.2f", settings.mouseSensitivity());
            case SFX_VOLUME -> Math.round(settings.sfxVolume() * 100f) + "%";
            case AMBIENCE_VOLUME -> Math.round(settings.ambienceVolume() * 100f) + "%";
            case PERFORMANCE -> settings.showPerformanceStats() ? I18n.text("common.on") : I18n.text("common.off");
            case AUTOSAVE -> I18n.text("common.seconds", settings.autosaveSeconds());
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
                apply(GameSettings.defaults().withLanguage(settings.language()));
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
            case SFX_VOLUME -> apply(settings.withSfxVolume(settings.sfxVolume() + 0.10f * Integer.signum(direction)));
            case AMBIENCE_VOLUME -> apply(settings.withAmbienceVolume(settings.ambienceVolume() + 0.10f * Integer.signum(direction)));
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
                apply(GameSettings.defaults().withLanguage(settings.language()));
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

    private void drawCentered(String value, float centerX, float baselineY) {
        glyphLayout.setText(font, value);
        font.draw(batch, value, centerX - glyphLayout.width * 0.5f, baselineY);
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
