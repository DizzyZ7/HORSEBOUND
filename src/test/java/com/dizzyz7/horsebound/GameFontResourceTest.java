// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameFontResourceTest {
    @Test
    void packagedBitmapFontContainsLatinAndCyrillicGlyphs() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        try (InputStream descriptor = loader.getResourceAsStream("fonts/horsebound-ui.fnt");
             InputStream atlas = loader.getResourceAsStream("fonts/horsebound-ui.png")) {
            assertNotNull(descriptor);
            assertNotNull(atlas);
            String text = new String(descriptor.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(text.contains("char id=65 "));   // A
            assertTrue(text.contains("char id=1040 ")); // А
            assertTrue(text.contains("char id=1072 ")); // а
            assertTrue(atlas.readAllBytes().length > 20_000);
        }
    }
}
