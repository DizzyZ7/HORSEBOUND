// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

record GlyphBinding(String glyph, String actionLabel) {
    GlyphBinding {
        glyph = glyph == null || glyph.isBlank() ? "?" : glyph.trim();
        actionLabel = actionLabel == null ? "" : actionLabel.trim();
    }
}
