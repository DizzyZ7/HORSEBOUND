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
    private static final int BACK_INDEX = 3;
    private static final int ITEM_COUNT = 4;

    private final HorseboundGame game;
    private final Mode mode;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final MenuInputMapper menuInput = new MenuInputMapper();
    private final Rectangle[] slotButtons = {new Rectangle(), new Rectangle(), new Rectangle()};
    private final Rectangle backButton = new Rectangle();

    private List<SaveSlotInfo> slots = List.of();
    private String pendingOverwriteSlot;
    private String message;
    private int selectedIndex;

    SaveSlotsScreen(HorseboundGame game, Mode mode) {
        this.game = game;
        this.mode = mode;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        slots = game.saveSlots();
        selectedIndex = 0;
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

        MenuInputSnapshot input = menuInput.sample();
        if (handleNavigation(input.command())) return;
        if (handlePointer(height)) return;

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.10f, 0.16f, 0.14f, 1f));
        shapes.rect(0f, 0f, width, height);

        for (int i = 0; i < slotButtons.length; i++) {
            shapes.setColor(slotColor(slots.get(i), i == selectedIndex));
            Rectangle rect = slotButtons[i];
            shapes.rect(rect.x, rect.y, rect.width, rect.height);
        }

        shapes.setColor(selectedIndex == BACK_INDEX
            ? new Color(0.28f, 0.52f, 0.36f, 1f)
            : new Color(0.16f, 0.21f, 0.18f, 1f));
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
                ? "Choose a world slot. Existing ranches require a second confirmation to overwrite."
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

        font.getData().setScale(0.78f);
        font.setColor(new Color(0.68f, 0.76f, 0.70f, 1f));
        font.draw(batch, inputHint(input.activeDevice()), centerX - 235f, 78f);

        if (message != null) {
            font.getData().setScale(0.85f);
            font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
            font.draw(batch, message, centerX - 240f, 52f);
        }

        font.getData().setScale(0.72f);
        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.draw(batch, "Created by Dimash Janibekov (DizZyZ7)", 18f, 20f);
        batch.end();
    }

    private boolean handleNavigation(MenuCommand command) {
        if (command.upPressed()) selectedIndex = Math.floorMod(selectedIndex - 1, ITEM_COUNT);
        if (command.downPressed()) selectedIndex = Math.floorMod(selectedIndex + 1, ITEM_COUNT);
        if (command.confirmPressed()) {
            if (selectedIndex == BACK_INDEX) game.returnToMenu();
            else activateSlot(selectedIndex);
            return true;
        }
        if (command.backPressed()) {
            game.returnToMenu();
            return true;
        }
        return false;
    }

    private boolean handlePointer(int height) {
        if (!Gdx.input.justTouched()) return false;
        menuInput.markPointerActive();
        float x = Gdx.input.getX();
        float y = height - Gdx.input.getY();
        for (int i = 0; i < slotButtons.length; i++) {
            if (slotButtons[i].contains(x, y)) {
                selectedIndex = i;
                activateSlot(i);
                return true;
            }
        }
        if (backButton.contains(x, y)) {
            selectedIndex = BACK_INDEX;
            game.returnToMenu();
            return true;
        }
        return false;
    }

    private Color slotColor(SaveSlotInfo slot, boolean selected) {
        if (selected) return new Color(0.28f, 0.52f, 0.36f, 1f);
        return switch (slot.state()) {
            case READY -> new Color(0.17f, 0.31f, 0.23f, 1f);
            case CORRUPT -> new Color(0.35f, 0.17f, 0.16f, 1f);
            case EMPTY -> new Color(0.15f, 0.20f, 0.17f, 1f);
        };
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
        if (index < 0 || index >= slots.size()) return;
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
            message = "Select " + slot.label() + " again to permanently replace that ranch.";
            return;
        }

        game.startNewWorld(slot.slotId());
    }

    private static String inputHint(InputDeviceType device) {
        return device == InputDeviceType.KEYBOARD_MOUSE
            ? "Up/Down or W/S navigate | Enter confirm | Esc back"
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
