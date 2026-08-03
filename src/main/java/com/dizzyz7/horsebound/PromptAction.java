// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum PromptAction {
    NAVIGATE("prompt.action.navigate"),
    ADJUST("prompt.action.adjust"),
    CONFIRM("prompt.action.confirm"),
    BACK("prompt.action.back"),
    INTERACT("prompt.action.interact"),
    MOUNT("prompt.action.mount"),
    BUILD("prompt.action.build"),
    INVENTORY("prompt.action.inventory"),
    JUMP("prompt.action.jump"),
    SPRINT("prompt.action.sprint"),
    SAVE("prompt.action.save"),
    PAUSE("prompt.action.pause");

    private final String labelKey;

    PromptAction(String labelKey) {
        this.labelKey = labelKey;
    }

    String label() { return I18n.text(labelKey); }
}
