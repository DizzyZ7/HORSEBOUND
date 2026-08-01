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

final class InputSettingsScreen implements Screen {
    private static final int INVERT_Y = 0;
    private static final int MOVE_DEAD_ZONE = 1;
    private static final int LOOK_DEAD_ZONE = 2;
    private static final int SPRINT_MODE = 3;
    private static final int RUMBLE = 4;
    private static final int RUMBLE_STRENGTH = 5;
    private static final int KEY_BINDINGS = 6;
    private static final int DEFAULTS = 7;
    private static final int BACK = 8;
    private static final int ITEM_COUNT = 9;

    private final HorseboundGame game;
    private final LivingRanchScreen pausedWorld;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final MenuInputMapper menuInput = new MenuInputMapper();
    private final Rectangle[] rows = new Rectangle[ITEM_COUNT];
    private InputProfile profile;
    private int selectedIndex;

    InputSettingsScreen(HorseboundGame game, LivingRanchScreen pausedWorld) {
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
        float geometry = Math.min(ui, 1.12f);
        float rowWidth = Math.min(width - 36f * geometry, 720f * geometry);
        float rowHeight = 42f * geometry;
        float gap = 48f * geometry;
        float x = (width - rowWidth) * 0.5f;
        float startY = height - 112f * geometry;
        for (int i = 0; i < rows.length; i++) rows[i].set(x, startY - i * gap, rowWidth, rowHeight);

        MenuInputSnapshot input = menuInput.sample();
        if (input.command().upPressed()) selectedIndex = Math.floorMod(selectedIndex - 1, ITEM_COUNT);
        if (input.command().downPressed()) selectedIndex = Math.floorMod(selectedIndex + 1, ITEM_COUNT);
        if (input.command().leftPressed()) adjust(-1);
        if (input.command().rightPressed()) adjust(1);
        if (input.command().confirmPressed()) {
            if (selectedIndex == KEY_BINDINGS) {
                game.showKeyBindings(pausedWorld);
                return;
            }
            if (selectedIndex == DEFAULTS) apply(InputProfile.defaults());
            else if (selectedIndex == BACK) {
                goBack();
                return;
            } else adjust(1);
        }
        if (input.command().backPressed()) {
            goBack();
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
        font.getData().setScale(2.15f * ui);
        font.draw(batch, "INPUT & ACCESSIBILITY", width * 0.5f - 245f * ui, height - 35f * geometry);
        font.getData().setScale(0.91f * ui);
        for (int i = 0; i < rows.length; i++) drawRow(i, rows[i], ui);
        font.getData().setScale(0.67f * ui);
        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.draw(batch, "Stored locally in input.properties and excluded from ranch saves.", 18f * geometry, 48f * geometry);
        batch.end();
    }

    private void drawRow(int index, Rectangle row, float ui) {
        font.setColor(index == selectedIndex ? Color.WHITE : new Color(0.84f, 0.90f, 0.85f, 1f));
        font.draw(batch, label(index), row.x + 14f * ui, row.y + 28f * ui);
        String value = value(index);
        if (!value.isEmpty()) {
            font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
            font.draw(batch, value, row.x + row.width - 205f * ui, row.y + 28f * ui);
        }
    }

    private String label(int index) {
        return switch (index) {
            case INVERT_Y -> "Invert vertical camera";
            case MOVE_DEAD_ZONE -> "Movement stick dead zone";
            case LOOK_DEAD_ZONE -> "Camera stick dead zone";
            case SPRINT_MODE -> "Sprint / gallop mode";
            case RUMBLE -> "Controller rumble";
            case RUMBLE_STRENGTH -> "Rumble strength";
            case KEY_BINDINGS -> "Keyboard bindings";
            case DEFAULTS -> "Restore input defaults";
            case BACK -> "Back";
            default -> throw new IllegalArgumentException("Unknown input setting: " + index);
        };
    }

    private String value(int index) {
        return switch (index) {
            case INVERT_Y -> profile.invertCameraY() ? "ON" : "OFF";
            case MOVE_DEAD_ZONE -> String.format(Locale.ROOT, "%.2f", profile.moveDeadZone());
            case LOOK_DEAD_ZONE -> String.format(Locale.ROOT, "%.2f", profile.lookDeadZone());
            case SPRINT_MODE -> profile.sprintMode().displayName();
            case RUMBLE -> profile.rumbleEnabled() ? "ON" : "OFF";
            case RUMBLE_STRENGTH -> Math.round(profile.rumbleStrength() * 100f) + "%";
            case KEY_BINDINGS -> "OPEN";
            case DEFAULTS, BACK -> "";
            default -> throw new IllegalArgumentException("Unknown input setting: " + index);
        };
    }

    private void adjust(int direction) {
        if (direction == 0) return;
        InputProfile next = switch (selectedIndex) {
            case INVERT_Y -> profile.withInvertCameraY(!profile.invertCameraY());
            case MOVE_DEAD_ZONE -> profile.withMoveDeadZone(profile.moveDeadZone() + 0.02f * direction);
            case LOOK_DEAD_ZONE -> profile.withLookDeadZone(profile.lookDeadZone() + 0.02f * direction);
            case SPRINT_MODE -> profile.withSprintMode(profile.sprintMode().toggled());
            case RUMBLE -> profile.withRumbleEnabled(!profile.rumbleEnabled());
            case RUMBLE_STRENGTH -> profile.withRumbleStrength(profile.rumbleStrength() + 0.10f * direction);
            default -> profile;
        };
        if (next != profile) {
            apply(next);
            if (selectedIndex == RUMBLE || selectedIndex == RUMBLE_STRENGTH) {
                ControllerRumble.pulse(profile, 80, 0.70f);
            }
        }
    }

    private void apply(InputProfile next) {
        game.updateInputProfile(next);
        profile = game.inputProfile();
    }

    private void goBack() {
        if (pausedWorld == null) game.showSettings();
        else game.showPause(pausedWorld);
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
            if (i == KEY_BINDINGS) game.showKeyBindings(pausedWorld);
            else if (i == DEFAULTS) apply(InputProfile.defaults());
            else if (i == BACK) goBack();
            else adjust(x < row.x + row.width * 0.5f ? -1 : 1);
            return true;
        }
        return false;
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
