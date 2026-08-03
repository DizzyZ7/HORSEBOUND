// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/** Unicode bitmap font shared by Russian and English UI. */
final class GameFonts {
    private static final float LEGACY_SCALE = 15f / 32f;
    private static final String FONT_PATH = "fonts/horsebound-ui.fnt";

    private GameFonts() {
    }

    static BitmapFont create() {
        BitmapFont font = new BitmapFont(Gdx.files.internal(FONT_PATH), false);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);
        setScale(font, 1f);
        return font;
    }

    static void setScale(BitmapFont font, float legacyScale) {
        font.getData().setScale(Math.max(0.01f, legacyScale) * LEGACY_SCALE);
    }
}
