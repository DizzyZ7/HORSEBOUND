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
    private final Rectangle playButton = new Rectangle();
    private final Rectangle exitButton = new Rectangle();

    MenuScreen(HorseboundGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        playButton.set(width * 0.5f - 130f, height * 0.5f - 10f, 260f, 58f);
        exitButton.set(width * 0.5f - 130f, height * 0.5f - 86f, 260f, 58f);

        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        shapes.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.12f, 0.18f, 0.16f, 1f));
        shapes.rect(0, 0, width, height);
        shapes.setColor(new Color(0.18f, 0.34f, 0.25f, 1f));
        shapes.rect(playButton.x, playButton.y, playButton.width, playButton.height);
        shapes.setColor(new Color(0.16f, 0.20f, 0.18f, 1f));
        shapes.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(3.3f);
        font.draw(batch, "HORSEBOUND", width * 0.5f - 175f, height * 0.72f);

        font.getData().setScale(1.05f);
        font.setColor(new Color(0.80f, 0.90f, 0.82f, 1f));
        font.draw(batch, "A cozy 3D horse sandbox", width * 0.5f - 115f, height * 0.72f - 55f);

        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);
        font.draw(batch, "PLAY", playButton.x + 96f, playButton.y + 38f);
        font.draw(batch, "EXIT", exitButton.x + 100f, exitButton.y + 38f);

        font.getData().setScale(0.9f);
        font.setColor(new Color(0.75f, 0.80f, 0.76f, 1f));
        font.draw(batch, "Created by Dimash Janibekov (DizZyZ7)", 22f, 38f);
        font.draw(batch, "(c) 2026 DizZyZ7. All rights reserved.", 22f, 20f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.startWorld();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }
        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = height - Gdx.input.getY();
            if (playButton.contains(x, y)) {
                game.startWorld();
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
