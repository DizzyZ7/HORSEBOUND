// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.List;

/** Derived, save-free guidance for the first complete ranch gameplay loop. */
final class RanchGuidance {
    private RanchGuidance() {
    }

    static Objective next(GameSession session, List<RanchWorldAccess.HorseTelemetry> horses) {
        if (session == null) return new Objective("guidance.explore", "guidance.explore.detail");
        boolean hasFeeder = has(session, HomesteadStructureType.FEEDER);
        boolean hasTrough = has(session, HomesteadStructureType.WATER_TROUGH);
        boolean feederStocked = stocked(session, HomesteadStructureType.FEEDER);
        boolean troughStocked = stocked(session, HomesteadStructureType.WATER_TROUGH);
        boolean hasTamed = horses != null && horses.stream().anyMatch(RanchWorldAccess.HorseTelemetry::tamed);
        boolean mounted = horses != null && horses.stream().anyMatch(RanchWorldAccess.HorseTelemetry::mounted);

        if (session.inventory().count(ItemId.WOOD) < 6 && !hasFeeder) {
            return new Objective("guidance.gather_wood", "guidance.gather_wood.detail");
        }
        if (!hasFeeder) return new Objective("guidance.build_feeder", "guidance.build_feeder.detail");
        if (!feederStocked) return new Objective("guidance.stock_feeder", "guidance.stock_feeder.detail");
        if (!hasTrough) return new Objective("guidance.build_trough", "guidance.build_trough.detail");
        if (!troughStocked) return new Objective("guidance.fill_trough", "guidance.fill_trough.detail");
        if (!hasTamed) return new Objective("guidance.befriend_horse", "guidance.befriend_horse.detail");
        if (!mounted) return new Objective("guidance.mount_horse", "guidance.mount_horse.detail");
        return new Objective("guidance.complete", "guidance.complete.detail");
    }

    private static boolean has(GameSession session, HomesteadStructureType type) {
        return session.homestead().structures().stream().anyMatch(value -> value.type() == type);
    }

    private static boolean stocked(GameSession session, HomesteadStructureType type) {
        return session.homestead().structures().stream()
            .anyMatch(value -> value.type() == type && value.storedUnits() > 0);
    }

    record Objective(String titleKey, String detailKey) {
        Objective {
            if (titleKey == null || titleKey.isBlank()) throw new IllegalArgumentException("titleKey is required");
            if (detailKey == null || detailKey.isBlank()) throw new IllegalArgumentException("detailKey is required");
        }

        String title() {
            return I18n.text(titleKey);
        }

        String detail() {
            return I18n.text(detailKey);
        }
    }
}
