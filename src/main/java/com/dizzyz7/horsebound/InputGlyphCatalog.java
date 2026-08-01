// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.List;

/** Maps semantic actions to the currently active keyboard or controller glyph family. */
final class InputGlyphCatalog {
    private InputGlyphCatalog() {
    }

    static GlyphBinding binding(PromptAction action, InputDeviceType device, ControllerGlyphFamily family) {
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
                PromptAction.SAVE,
                PromptAction.PAUSE
            );
        }
        if (screen instanceof InputSettingsScreen || screen instanceof SettingsScreen) {
            return List.of(PromptAction.NAVIGATE, PromptAction.ADJUST, PromptAction.CONFIRM, PromptAction.BACK);
        }
        return List.of(PromptAction.NAVIGATE, PromptAction.CONFIRM, PromptAction.BACK);
    }

    private static String keyboardGlyph(PromptAction action) {
        InputProfile profile = InputProfileContext.current();
        return switch (action) {
            case NAVIGATE -> "WASD";
            case ADJUST -> "A / D";
            case CONFIRM -> "ENTER";
            case BACK -> "ESC";
            case PAUSE -> KeyLabel.of(profile.pauseKey());
            case INTERACT -> KeyLabel.of(profile.interactKey());
            case MOUNT -> KeyLabel.of(profile.mountKey());
            case BUILD -> KeyLabel.of(profile.buildKey());
            case JUMP -> KeyLabel.of(profile.jumpKey());
            case SPRINT -> KeyLabel.of(profile.sprintKey());
            case SAVE -> KeyLabel.of(profile.saveKey());
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
        return switch (family) {
            case XBOX, STEAM_DECK -> "LB";
            case NINTENDO -> "L";
            default -> "L1";
        };
    }

    private static String shoulderRight(ControllerGlyphFamily family) {
        return switch (family) {
            case XBOX, STEAM_DECK -> "RB";
            case NINTENDO -> "R";
            default -> "R1";
        };
    }

    private static String viewButton(ControllerGlyphFamily family) {
        return switch (family) {
            case PLAYSTATION -> "SHARE";
            case STEAM_DECK, XBOX -> "VIEW";
            case NINTENDO -> "MINUS";
            case GENERIC -> "BACK";
        };
    }
}
