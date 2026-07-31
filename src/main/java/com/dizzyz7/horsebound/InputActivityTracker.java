// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

/**
 * Shared prompt state. Input adapters record only meaningful activity, so stick drift does not flicker glyphs.
 */
final class InputActivityTracker {
    private static InputDeviceType activeDevice = InputDeviceType.KEYBOARD_MOUSE;
    private static ControllerGlyphFamily controllerFamily = ControllerGlyphFamily.GENERIC;

    private InputActivityTracker() {
    }

    static void record(InputDeviceType device) {
        if (device == null) return;
        activeDevice = device;
        if (device == InputDeviceType.GAMEPAD || device == InputDeviceType.STEAM_INPUT) {
            controllerFamily = GdxControllerGlyphResolver.currentFamily();
        }
    }

    static void record(InputDeviceType device, ControllerGlyphFamily family) {
        if (device == null) return;
        activeDevice = device;
        if (device == InputDeviceType.GAMEPAD || device == InputDeviceType.STEAM_INPUT) {
            controllerFamily = family == null ? ControllerGlyphFamily.GENERIC : family;
        }
    }

    static InputDeviceType activeDevice() {
        return activeDevice;
    }

    static ControllerGlyphFamily controllerFamily() {
        return controllerFamily;
    }

    static void reset() {
        activeDevice = InputDeviceType.KEYBOARD_MOUSE;
        controllerFamily = ControllerGlyphFamily.GENERIC;
    }
}
