// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/** Single-player runtime profile shared by device adapters on the render thread. */
final class InputProfileContext {
    private static volatile InputProfile current = InputProfile.defaults();

    private InputProfileContext() {
    }

    static InputProfile current() {
        return current;
    }

    static void set(InputProfile profile) {
        current = profile == null ? InputProfile.defaults() : profile;
    }

    static void reset() {
        current = InputProfile.defaults();
    }
}
