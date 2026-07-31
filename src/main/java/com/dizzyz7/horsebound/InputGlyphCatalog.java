// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.List;

/** Maps semantic actions to the currently active keyboard or controller glyph family. */
final class InputGlyphCatalog {
    private InputGlyphCatalog() {
    }

    static GlyphBinding binding(
        PromptAction action,
        InputDeviceType device,
        ControllerGlyphFamily family
    ) {
        if (device == InputDeviceType.KEYBOARD_MOUSE) {
            return new GlyphBinding(keyboardGlyph(action), action.label());
        }
        ControllerGlyphFamily safeFamily = family == null ? ControllerGlyphFamily.GENERIC : family;
        return new GlyphBinding(controllerGlyph(action, safeFamily), action.label());
    }

    static List<PromptAction> actionsForScreen(Object screen) {
        if (screen instanceof LivingRanchScreen) {
            return List.of(
                PromptAction.INTERACT,
                PromptAction.MOUNT,
                PromptAction.JUMP,
                PromptAction.SPRINT,
                PromptAction.BUILD,
                PromptAction.PAUSE
            );
        }
        if (screen instanceof SettingsScreen) {
            return List.of(PromptAction.NAVIGATE, PromptAction.ADJUST, PromptAction.CONFIRM, PromptAction.BACK);
        }
        if (screen instanceof SaveSlotsScreen) {
            return List.of(PromptAction.NAVIGATE, PromptAction.CONFIRM, PromptAction.BACK);
        }
        return List.of(PromptAction.NAVIGATE, PromptAction.CONFIRM, PromptAction.BACK);
    }

    private static String keyboardGlyph(PromptAction action) {
        return switch (action) {
            case NAVIGATE -> "WASD";
            case ADJUST -> "A / D";
            case CONFIRM -> "ENTER";
            case BACK, PAUSE -> "ESC";
            case INTERACT -> "E";
            case MOUNT -> "F";
            case BUILD -> "B";
            case JUMP -> "SPACE";
            case SPRINT -> "SHIFT";
            case SAVE -> "F5";
        };
    }

    private static String controllerGlyph(PromptAction action, ControllerGlyphFamily family) {
        return switch (action) {
            case NAVIGATE -> "D-PAD / LS";
            case ADJUST -> "LEFT / RIGHT";
            case CONFIRM, JUMP -> faceA(family);
            case BACK, PAUSE -> faceB(family);
            case INTERACT -> faceX(family);
            case MOUNT -> faceY(family);
            case BUILD -> shoulderLeft(family);
            case SPRINT -> shoulderRight(family);
            case SAVE -> viewButton(family);
        };
    }

    private static String faceA(ControllerGlyphFamily family) {
        return family == ControllerGlyphFamily.PLAYSTATION ? "CROSS" : "A";
    }

    private static String faceB(ControllerGlyphFamily family) {
        return family == ControllerGlyphFamily.PLAYSTATION ? "CIRCLE" : "B";
    }

    private static String faceX(ControllerGlyphFamily family) {
        return family == ControllerGlyphFamily.PLAYSTATION ? "SQUARE" : "X";
    }

    private static String faceY(ControllerGlyphFamily family) {
        return family == ControllerGlyphFamily.PLAYSTATION ? "TRIANGLE" : "Y";
    }

    private static String shoulderLeft(ControllerGlyphFamily family) {
        return family == ControllerGlyphFamily.XBOX || family == ControllerGlyphFamily.STEAM_DECK ? "LB" : "L1";
    }

    private static String shoulderRight(ControllerGlyphFamily family) {
        return family == ControllerGlyphFamily.XBOX || family == ControllerGlyphFamily.STEAM_DECK ? "RB" : "R1";
    }

    private static String viewButton(ControllerGlyphFamily family) {
        return switch (family) {
            case PLAYSTATION -> "SHARE";
            case STEAM_DECK -> "VIEW";
            case XBOX -> "VIEW";
            case GENERIC -> "BACK";
        };
    }
}
