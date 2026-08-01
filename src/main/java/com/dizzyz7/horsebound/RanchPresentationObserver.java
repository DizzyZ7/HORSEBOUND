// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Detects presentation-only structure changes without coupling domain state to libGDX audio. */
final class RanchPresentationObserver {
    private final Map<UUID, Snapshot> previous = new LinkedHashMap<>();
    private boolean initialized;

    List<RanchAudio.Cue> observe(List<PlacedStructure> structures) {
        Map<UUID, Snapshot> current = new LinkedHashMap<>();
        if (structures != null) {
            for (PlacedStructure structure : structures) {
                if (structure == null) continue;
                current.put(structure.id(), Snapshot.from(structure));
            }
        }

        if (!initialized) {
            initialized = true;
            previous.clear();
            previous.putAll(current);
            return List.of();
        }

        List<RanchAudio.Cue> events = new ArrayList<>();
        for (Map.Entry<UUID, Snapshot> entry : previous.entrySet()) {
            if (!current.containsKey(entry.getKey())) events.add(RanchAudio.Cue.DISMANTLE);
        }
        for (Map.Entry<UUID, Snapshot> entry : current.entrySet()) {
            Snapshot old = previous.get(entry.getKey());
            Snapshot now = entry.getValue();
            if (old == null) {
                events.add(RanchAudio.Cue.BUILD);
                continue;
            }
            if (old.open != now.open && now.type == HomesteadStructureType.GATE) {
                events.add(now.open ? RanchAudio.Cue.GATE_OPEN : RanchAudio.Cue.GATE_CLOSE);
            }
            if (changed(old.x, now.x) || changed(old.z, now.z) || changed(old.heading, now.heading)) {
                events.add(RanchAudio.Cue.MOVE);
            }
        }

        previous.clear();
        previous.putAll(current);
        return List.copyOf(events);
    }

    int trackedCount() {
        return previous.size();
    }

    private static boolean changed(float left, float right) {
        return Math.abs(left - right) > 0.001f;
    }

    private record Snapshot(
        HomesteadStructureType type,
        float x,
        float z,
        float heading,
        boolean open
    ) {
        private static Snapshot from(PlacedStructure structure) {
            return new Snapshot(
                structure.type(),
                structure.x(),
                structure.z(),
                structure.heading(),
                structure.isOpen()
            );
        }
    }
}
