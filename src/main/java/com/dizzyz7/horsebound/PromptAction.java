// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

enum PromptAction {
    NAVIGATE("Navigate"),
    ADJUST("Adjust"),
    CONFIRM("Confirm"),
    BACK("Back"),
    INTERACT("Interact"),
    MOUNT("Mount"),
    BUILD("Build / Edit"),
    INVENTORY("Inventory"),
    JUMP("Jump"),
    SPRINT("Sprint / Gallop"),
    SAVE("Save"),
    PAUSE("Pause");

    private final String label;

    PromptAction(String label) {
        this.label = label;
    }

    String label() {
        return label;
    }
}
