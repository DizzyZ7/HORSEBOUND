// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import java.util.List;

/** Deck-safe in-ranch inventory and chest transfer overlay. */
final class InventoryOverlay implements Disposable {
    private static final int ROWS_VISIBLE = 8;

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final MenuInputMapper input = new MenuInputMapper();
    private final InventoryTransferService transfers = new InventoryTransferService();

    private Inventory player;
    private PlacedStructure chest;
    private boolean open;
    private boolean chestPanel;
    private int selectedIndex;
    private String message = "Confirm: one | Build/L1: stack | Mount/Y: all.";

    void open(Inventory playerInventory, PlacedStructure chestStructure) {
        player = playerInventory;
        chest = chestStructure != null && chestStructure.type().storesItems() ? chestStructure : null;
        chestPanel = false;
        selectedIndex = 0;
        open = true;
        message = chest == null
            ? "Player inventory. Back closes."
            : "Confirm: one | Build/L1: stack | Mount/Y: all.";
        Gdx.input.setCursorCatched(false);
    }

    boolean isOpen() {
        return open;
    }

    void close() {
        open = false;
        chest = null;
        Gdx.input.setCursorCatched(true);
    }

    void updateAndRender(float uiScale) {
        if (!open || player == null) return;
        MenuCommand command = input.sample().command();
        List<InventoryStack> current = currentStacks();
        if (command.leftPressed() && chest != null) switchPanel(false);
        if (command.rightPressed() && chest != null) switchPanel(true);
        current = currentStacks();
        if (command.upPressed()) selectedIndex = Math.floorMod(selectedIndex - 1, Math.max(1, current.size()));
        if (command.downPressed()) selectedIndex = Math.floorMod(selectedIndex + 1, Math.max(1, current.size()));

        if (HomesteadActionBus.consumeDismantle()) {
            transferSelected(InventoryTransferService.TransferMode.ALL);
        } else if (HomesteadActionBus.consumeBuild()) {
            transferSelected(InventoryTransferService.TransferMode.STACK);
        } else if (command.confirmPressed()) {
            transferSelected(InventoryTransferService.TransferMode.ONE);
        }

        if (command.backPressed()) {
            close();
            return;
        }
        render(uiScale);
    }

    private void switchPanel(boolean toChest) {
        chestPanel = toChest;
        selectedIndex = 0;
    }

    private void transferSelected(InventoryTransferService.TransferMode mode) {
        List<InventoryStack> stacks = currentStacks();
        if (stacks.isEmpty()) {
            message = "This inventory is empty.";
            return;
        }
        selectedIndex = Math.min(selectedIndex, stacks.size() - 1);
        InventoryStack selected = stacks.get(selectedIndex);
        ItemId item = selected.item();
        if (chest == null) {
            message = "Open a nearby Chest with Interact to transfer items.";
            return;
        }

        Inventory source = chestPanel ? chest.itemStorage() : player;
        Inventory destination = chestPanel ? player : chest.itemStorage();
        InventoryTransferService.TransferResult result = transfers.transfer(
            source,
            destination,
            item,
            selected.amount(),
            mode
        );
        message = switch (result.status()) {
            case SUCCESS -> transferSuccessMessage(mode, item, result.moved());
            case FULL -> chestPanel ? "Player inventory has insufficient space." : "Chest has insufficient space.";
            case NO_ITEM -> "That item is no longer available.";
        };
        List<InventoryStack> after = currentStacks();
        selectedIndex = after.isEmpty() ? 0 : Math.min(selectedIndex, after.size() - 1);
    }

    private String transferSuccessMessage(
        InventoryTransferService.TransferMode mode,
        ItemId item,
        int moved
    ) {
        RanchAudio.play(RanchAudio.Cue.INVENTORY_TRANSFER);
        String verb = chestPanel ? "Took " : "Stored ";
        String amount = mode == InventoryTransferService.TransferMode.ONE
            ? "1"
            : Integer.toString(moved);
        return verb + amount + " " + item.displayName() + ".";
    }

    private List<InventoryStack> currentStacks() {
        if (chestPanel && chest != null) return chest.itemStorage().stackView();
        return player == null ? List.of() : player.stackView();
    }

    private void render(float requestedScale) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, requestedScale);
        float geometry = Math.min(ui, 1.18f);
        float panelWidth = Math.min(width - 42f * geometry, 860f * geometry);
        float panelHeight = Math.min(height - 70f * geometry, 570f * geometry);
        float x = (width - panelWidth) * 0.5f;
        float y = (height - panelHeight) * 0.5f;
        float half = panelWidth * 0.5f;

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0f, 0f, 0f, 0.58f));
        shapes.rect(0f, 0f, width, height);
        shapes.setColor(new Color(0.07f, 0.11f, 0.09f, 0.98f));
        shapes.rect(x, y, panelWidth, panelHeight);
        drawPanelRows(x + 18f * geometry, y + 72f * geometry, half - 28f * geometry, player.stackView(), !chestPanel, geometry);
        if (chest != null) {
            drawPanelRows(x + half + 10f * geometry, y + 72f * geometry, half - 28f * geometry, chest.itemStorage().stackView(), chestPanel, geometry);
        }
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.35f * ui);
        font.draw(batch, "INVENTORY", x + 18f * geometry, y + panelHeight - 22f * geometry);
        font.getData().setScale(0.82f * ui);
        font.setColor(!chestPanel ? Color.WHITE : new Color(0.65f, 0.72f, 0.67f, 1f));
        font.draw(batch, "BACKPACK  " + player.usedSlots() + "/" + player.slotCapacity(), x + 18f * geometry, y + panelHeight - 62f * geometry);
        if (chest != null) {
            font.setColor(chestPanel ? Color.WHITE : new Color(0.65f, 0.72f, 0.67f, 1f));
            font.draw(
                batch,
                "CHEST  " + chest.itemStorage().usedSlots() + "/" + chest.itemStorage().slotCapacity(),
                x + half + 10f * geometry,
                y + panelHeight - 62f * geometry
            );
        }
        drawRowsText(x + 18f * geometry, y + 72f * geometry, player.stackView(), !chestPanel, ui, geometry);
        if (chest != null) {
            drawRowsText(x + half + 10f * geometry, y + 72f * geometry, chest.itemStorage().stackView(), chestPanel, ui, geometry);
        }
        font.getData().setScale(0.72f * ui);
        font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
        font.draw(batch, message, x + 18f * geometry, y + 36f * geometry);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private void drawPanelRows(
        float x,
        float y,
        float width,
        List<InventoryStack> stacks,
        boolean active,
        float geometry
    ) {
        int start = scrollStart(stacks.size());
        for (int row = 0; row < ROWS_VISIBLE; row++) {
            int index = start + row;
            float rowY = y + (ROWS_VISIBLE - 1 - row) * 43f * geometry;
            boolean selected = active && index == selectedIndex;
            shapes.setColor(selected
                ? new Color(0.32f, 0.52f, 0.35f, 1f)
                : new Color(0.13f, 0.19f, 0.15f, 1f));
            shapes.rect(x, rowY, width, 36f * geometry);
        }
    }

    private void drawRowsText(
        float x,
        float y,
        List<InventoryStack> stacks,
        boolean active,
        float ui,
        float geometry
    ) {
        int start = scrollStart(stacks.size());
        font.getData().setScale(0.76f * ui);
        for (int row = 0; row < ROWS_VISIBLE; row++) {
            int index = start + row;
            if (index >= stacks.size()) continue;
            InventoryStack stack = stacks.get(index);
            float rowY = y + (ROWS_VISIBLE - 1 - row) * 43f * geometry + 25f * geometry;
            font.setColor(active && index == selectedIndex ? Color.WHITE : new Color(0.82f, 0.88f, 0.83f, 1f));
            font.draw(batch, stack.item().displayName(), x + 10f * geometry, rowY);
            font.setColor(new Color(1f, 0.88f, 0.58f, 1f));
            font.draw(batch, Integer.toString(stack.amount()), x + 250f * geometry, rowY);
        }
    }

    private int scrollStart(int size) {
        if (size <= ROWS_VISIBLE) return 0;
        return Math.max(0, Math.min(selectedIndex - ROWS_VISIBLE / 2, size - ROWS_VISIBLE));
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
