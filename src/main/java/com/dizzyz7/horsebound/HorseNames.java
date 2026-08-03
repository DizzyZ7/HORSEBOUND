// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

final class HorseNames {
    private HorseNames() {
    }

    static String display(String storedName) {
        if (storedName == null || storedName.isBlank()) return I18n.text("horse.name.unknown");
        return switch (storedName) {
            case "Ember" -> I18n.text("horse.name.ember");
            case "Willow" -> I18n.text("horse.name.willow");
            case "Comet" -> I18n.text("horse.name.comet");
            case "Hazel" -> I18n.text("horse.name.hazel");
            default -> storedName;
        };
    }
}
