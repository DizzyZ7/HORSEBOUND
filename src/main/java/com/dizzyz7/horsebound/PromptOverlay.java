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

import java.util.ArrayList;
import java.util.List;

/** Global Deck-safe prompt strip rendered after the active screen. */
final class PromptOverlay {
    private static final Color PANEL = new Color(0.035f, 0.055f, 0.05f, 0.94f);
    private static final Color CHIP = new Color(0.20f, 0.36f, 0.27f, 1f);
    private static final Color CHIP_CONTROLLER = new Color(0.24f, 0.31f, 0.48f, 1f);
    private static final Color TEXT = new Color(0.94f, 0.96f, 0.93f, 1f);
    private static final Color SECONDARY = new Color(0.72f, 0.79f, 0.73f, 1f);

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final String buildLabel = BuildInfo.current().displayLabel();

    void render(Screen screen, GameSettings settings) {
        if (screen == null || settings == null) return;
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, settings.uiScale());
        float geometryScale = Math.min(ui, 1.20f);
        InputDeviceType device = InputActivityTracker.activeDevice();
        ControllerGlyphFamily family = InputActivityTracker.controllerFamily();
        List<GlyphBinding> bindings = bindings(screen, device, family);

        float panelHeight = bindings.size() > 4 ? 66f * geometryScale : 42f * geometryScale;
        float panelY = 0f;

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(PANEL);
        shapes.rect(0f, panelY, width, panelHeight);

        if (screen instanceof LivingRanchScreen) {
            shapes.rect(0f, height - 52f * geometryScale, 350f * geometryScale, 52f * geometryScale);
        }

        List<PromptPosition> positions = layoutBindings(bindings, width, panelHeight, geometryScale, ui);
        shapes.setColor(device == InputDeviceType.KEYBOARD_MOUSE ? CHIP : CHIP_CONTROLLER);
        for (PromptPosition position : positions) {
            shapes.rect(position.chipX(), position.y(), position.chipWidth(), position.height());
        }
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        font.getData().setScale(0.72f * ui);
        font.setColor(TEXT);
        for (int i = 0; i < positions.size(); i++) {
            PromptPosition position = positions.get(i);
            GlyphBinding binding = bindings.get(i);
            layout.setText(font, binding.glyph());
            float glyphX = position.chipX() + (position.chipWidth() - layout.width) * 0.5f;
            font.draw(batch, binding.glyph(), glyphX, position.y() + position.height() * 0.72f);
            font.setColor(SECONDARY);
            font.draw(batch, binding.actionLabel(), position.textX(), position.y() + position.height() * 0.72f);
            font.setColor(TEXT);
        }

        if (screen instanceof LivingRanchScreen) {
            font.getData().setScale(0.84f * ui);
            font.setColor(TEXT);
            font.draw(batch, buildLabel, 14f * geometryScale, height - 18f * geometryScale);
            font.getData().setScale(0.64f * ui);
            font.setColor(SECONDARY);
            font.draw(
                batch,
                deviceLabel(device, family),
                14f * geometryScale,
                height - 39f * geometryScale
            );
        }
        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private List<GlyphBinding> bindings(
        Screen screen,
        InputDeviceType device,
        ControllerGlyphFamily family
    ) {
        List<GlyphBinding> result = new ArrayList<>();
        for (PromptAction action : InputGlyphCatalog.actionsForScreen(screen)) {
            result.add(InputGlyphCatalog.binding(action, device, family));
        }
        return result;
    }

    private List<PromptPosition> layoutBindings(
        List<GlyphBinding> bindings,
        int width,
        float panelHeight,
        float geometryScale,
        float ui
    ) {
        List<PromptPosition> result = new ArrayList<>(bindings.size());
        font.getData().setScale(0.72f * ui);
        float gap = 14f * geometryScale;
        float x = 14f * geometryScale;
        float y = panelHeight - 29f * geometryScale;
        float itemHeight = 22f * geometryScale;

        for (GlyphBinding binding : bindings) {
            layout.setText(font, binding.glyph());
            float chipWidth = Math.max(28f * geometryScale, layout.width + 14f * geometryScale);
            layout.setText(font, binding.actionLabel());
            float textWidth = layout.width;
            float itemWidth = chipWidth + 7f * geometryScale + textWidth;
            if (x + itemWidth > width - 14f * geometryScale && x > 14f * geometryScale) {
                x = 14f * geometryScale;
                y -= 28f * geometryScale;
            }
            result.add(new PromptPosition(
                x,
                x + chipWidth + 7f * geometryScale,
                y,
                chipWidth,
                itemHeight
            ));
            x += itemWidth + gap;
        }
        return result;
    }

    private static String deviceLabel(InputDeviceType device, ControllerGlyphFamily family) {
        if (device == InputDeviceType.KEYBOARD_MOUSE) return "Keyboard / Mouse prompts";
        return switch (family) {
            case PLAYSTATION -> "PlayStation controller prompts";
            case STEAM_DECK -> "Steam Deck controller prompts";
            case XBOX -> "Xbox controller prompts";
            case GENERIC -> "Generic controller prompts";
        };
    }

    void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }

    private record PromptPosition(
        float chipX,
        float textX,
        float y,
        float chipWidth,
        float height
    ) {
    }
}
