// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class SaveSlotsScreen implements Screen {
    enum Mode {
        NEW_GAME,
        LOAD_GAME
    }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault());

    private final HorseboundGame game;
    private final Mode mode;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final Rectangle[] slotButtons = {new Rectangle(), new Rectangle(), new Rectangle()};
    private final Rectangle backButton = new Rectangle();

    private List<SaveSlotInfo> slots = List.of();
    private String pendingOverwriteSlot;
    private String message;

    SaveSlotsScreen(HorseboundGame game, Mode mode) {
        this.game = game;
        this.mode = mode;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        slots = game.saveSlots();
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float centerX = width * 0.5f;
        float top = height * 0.68f;

        for (int i = 0; i < slotButtons.length; i++) {
            slotButtons[i].set(centerX - 260f, top - i * 104f, 520f, 82f);
        }
        backButton.set(centerX - 100f, top - 345f, 200f, 52f);

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.10f, 0.16f, 0.14f, 1f));
        shapes.rect(0f, 0f, width, height);

        for (int i = 0; i < slotButtons.length; i++) {
            SaveSlotInfo slot = slots.get(i);
            if (slot.state() == SaveSlotInfo.State.READY) {
                shapes.setColor(new Color(0.17f, 0.31f, 0.23f, 1f));
            } else if (slot.state() == SaveSlotInfo.State.CORRUPT) {
                shapes.setColor(new Color(0.35f, 0.17f, 0.16f, 1f));
            } else {
                shapes.setColor(new Color(0.15f, 0.20f, 0.17f, 1f));
            }
            shapes.rect(slotButtons[i].x, slotButtons[i].y, slotButtons[i].width, slotButtons[i].height);
        }

        shapes.setColor(new Color(0.16f, 0.21f, 0.18f, 1f));
        shapes.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(2.3f);
        font.draw(batch, mode == Mode.NEW_GAME ? "NEW RANCH" : "LOAD RANCH", centerX - 145f, height * 0.84f);

        font.getData().setScale(0.9f);
        font.setColor(new Color(0.76f, 0.84f, 0.78f, 1f));
        font.draw(batch,
            mode == Mode.NEW_GAME
                ? "Choose a world slot. Existing ranches require a second click to overwrite."
                : "Choose a saved ranch to continue.",
            centerX - 250f,
            height * 0.84f - 38f
        );

        for (int i = 0; i < slots.size(); i++) {
            drawSlot(slots.get(i), slotButtons[i], i + 1);
        }

        font.setColor(Color.WHITE);
        font.getData().setScale(1.05f);
        font.draw(batch, "BACK", backButton.x + 72f, backButton.y + 34f);

        if (message != null) {
            font.getData().setScale(0.85f);
            font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
            font.draw(batch, message, centerX - 240f, 56f);
        }

        font.getData().setScale(0.72f);
        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.draw(batch, "Created by Dimash Janibekov (DizZyZ7)", 18f, 20f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.returnToMenu();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            activateSlot(0);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            activateSlot(1);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            activateSlot(2);
            return;
        }

        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = height - Gdx.input.getY();
            for (int i = 0; i < slotButtons.length; i++) {
                if (slotButtons[i].contains(x, y)) {
                    activateSlot(i);
                    return;
                }
            }
            if (backButton.contains(x, y)) {
                game.returnToMenu();
            }
        }
    }

    private void drawSlot(SaveSlotInfo slot, Rectangle rect, int number) {
        font.getData().setScale(1.15f);
        font.setColor(Color.WHITE);
        font.draw(batch, number + ". " + slot.label(), rect.x + 18f, rect.y + 57f);

        font.getData().setScale(0.78f);
        if (slot.state() == SaveSlotInfo.State.EMPTY) {
            font.setColor(new Color(0.68f, 0.76f, 0.70f, 1f));
            font.draw(batch, "Empty slot", rect.x + 18f, rect.y + 29f);
            return;
        }
        if (slot.state() == SaveSlotInfo.State.CORRUPT) {
            font.setColor(new Color(1f, 0.62f, 0.56f, 1f));
            font.draw(batch, "Save and backup are unreadable", rect.x + 18f, rect.y + 29f);
            return;
        }

        font.setColor(new Color(0.74f, 0.84f, 0.76f, 1f));
        String saved = DATE_FORMAT.format(Instant.ofEpochMilli(slot.savedAtEpochMillis()));
        font.draw(batch,
            "Saved " + saved + " | horses " + slot.horseCount() + " | tamed " + slot.tamedHorseCount()
                + " | fences " + slot.fenceCount(),
            rect.x + 18f,
            rect.y + 29f
        );
    }

    private void activateSlot(int index) {
        if (index < 0 || index >= slots.size()) {
            return;
        }
        SaveSlotInfo slot = slots.get(index);

        if (mode == Mode.LOAD_GAME) {
            if (!slot.canLoad()) {
                message = slot.state() == SaveSlotInfo.State.CORRUPT
                    ? "That ranch cannot be loaded because both save copies are damaged."
                    : "That ranch slot is empty.";
                return;
            }
            game.loadWorld(slot.slotId());
            return;
        }

        if (slot.state() == SaveSlotInfo.State.EMPTY) {
            game.startNewWorld(slot.slotId());
            return;
        }

        if (!slot.slotId().equals(pendingOverwriteSlot)) {
            pendingOverwriteSlot = slot.slotId();
            message = "Click " + slot.label() + " again to permanently replace that ranch.";
            return;
        }

        game.startNewWorld(slot.slotId());
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}
