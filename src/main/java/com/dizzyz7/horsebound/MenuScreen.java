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

final class MenuScreen implements Screen {
    private final HorseboundGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final Rectangle continueButton = new Rectangle();
    private final Rectangle newGameButton = new Rectangle();
    private final Rectangle loadGameButton = new Rectangle();
    private final Rectangle settingsButton = new Rectangle();
    private final Rectangle exitButton = new Rectangle();

    private boolean canContinue;

    MenuScreen(HorseboundGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        canContinue = game.hasContinue();
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float centerX = width * 0.5f;
        float startY = height * 0.55f;

        continueButton.set(centerX - 150f, startY + 78f, 300f, 54f);
        newGameButton.set(centerX - 150f, startY + 12f, 300f, 54f);
        loadGameButton.set(centerX - 150f, startY - 54f, 300f, 54f);
        settingsButton.set(centerX - 150f, startY - 120f, 300f, 54f);
        exitButton.set(centerX - 150f, startY - 186f, 300f, 54f);

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.12f, 0.18f, 0.16f, 1f));
        shapes.rect(0, 0, width, height);

        shapes.setColor(canContinue
            ? new Color(0.18f, 0.34f, 0.25f, 1f)
            : new Color(0.12f, 0.15f, 0.14f, 1f));
        shapes.rect(continueButton.x, continueButton.y, continueButton.width, continueButton.height);

        shapes.setColor(new Color(0.21f, 0.40f, 0.29f, 1f));
        shapes.rect(newGameButton.x, newGameButton.y, newGameButton.width, newGameButton.height);

        shapes.setColor(new Color(0.17f, 0.29f, 0.22f, 1f));
        shapes.rect(loadGameButton.x, loadGameButton.y, loadGameButton.width, loadGameButton.height);
        shapes.rect(settingsButton.x, settingsButton.y, settingsButton.width, settingsButton.height);

        shapes.setColor(new Color(0.16f, 0.20f, 0.18f, 1f));
        shapes.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(3.3f);
        font.draw(batch, "HORSEBOUND", centerX - 175f, height * 0.86f);

        font.getData().setScale(1.05f);
        font.setColor(new Color(0.80f, 0.90f, 0.82f, 1f));
        font.draw(batch, "A cozy 3D horse sandbox", centerX - 115f, height * 0.86f - 55f);

        font.getData().setScale(1.16f);
        font.setColor(canContinue ? Color.WHITE : new Color(0.48f, 0.52f, 0.49f, 1f));
        font.draw(batch, "CONTINUE", continueButton.x + 92f, continueButton.y + 35f);
        font.setColor(Color.WHITE);
        font.draw(batch, "NEW GAME", newGameButton.x + 88f, newGameButton.y + 35f);
        font.draw(batch, "LOAD GAME", loadGameButton.x + 84f, loadGameButton.y + 35f);
        font.draw(batch, "SETTINGS", settingsButton.x + 94f, settingsButton.y + 35f);
        font.draw(batch, "EXIT", exitButton.x + 120f, exitButton.y + 35f);

        font.getData().setScale(0.78f);
        font.setColor(new Color(0.66f, 0.74f, 0.68f, 1f));
        font.draw(batch,
            canContinue ? "Continue opens the most recently saved ranch." : "No ranch yet. Start a new world.",
            centerX - 165f,
            startY - 224f
        );

        font.getData().setScale(0.9f);
        font.setColor(new Color(0.75f, 0.80f, 0.76f, 1f));
        font.draw(batch, "Created by Dimash Janibekov (DizZyZ7)", 22f, 38f);
        font.draw(batch, "(c) 2026 DizZyZ7. All rights reserved.", 22f, 20f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (canContinue) {
                game.continueWorld();
            } else {
                game.showNewGameSlots();
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            game.showNewGameSlots();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.showLoadGameSlots();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            game.showSettings();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = height - Gdx.input.getY();
            if (canContinue && continueButton.contains(x, y)) {
                game.continueWorld();
                return;
            }
            if (newGameButton.contains(x, y)) {
                game.showNewGameSlots();
                return;
            }
            if (loadGameButton.contains(x, y)) {
                game.showLoadGameSlots();
                return;
            }
            if (settingsButton.contains(x, y)) {
                game.showSettings();
                return;
            }
            if (exitButton.contains(x, y)) {
                Gdx.app.exit();
            }
        }
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
