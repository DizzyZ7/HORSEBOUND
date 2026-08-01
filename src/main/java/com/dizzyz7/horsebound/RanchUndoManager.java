// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * One-level transactional undo for player-authored Homestead placement and relocation.
 * Undo state is intentionally session-local and never persisted into ranch saves.
 */
final class RanchUndoManager {
    private Action pending;

    void recordPlacement(PlacedStructure structure) {
        Objects.requireNonNull(structure, "structure");
        pending = new Action(
            Kind.PLACEMENT,
            structure.id(),
            structure.type(),
            structure.x(),
            structure.z(),
            structure.heading(),
            structure.x(),
            structure.z(),
            structure.heading()
        );
    }

    void recordRelocation(
        PlacedStructure structure,
        float fromX,
        float fromZ,
        float fromHeading,
        float toX,
        float toZ,
        float toHeading
    ) {
        Objects.requireNonNull(structure, "structure");
        pending = new Action(
            Kind.RELOCATION,
            structure.id(),
            structure.type(),
            finiteOrZero(fromX),
            finiteOrZero(fromZ),
            normalizeHeading(fromHeading),
            finiteOrZero(toX),
            finiteOrZero(toZ),
            normalizeHeading(toHeading)
        );
    }

    boolean hasPending() {
        return pending != null;
    }

    Kind pendingKind() {
        return pending == null ? null : pending.kind();
    }

    UndoResult undo(HomesteadState state, Inventory inventory, RelocationValidator validator) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(validator, "validator");
        if (pending == null) return UndoResult.NOTHING_TO_UNDO;

        Action action = pending;
        PlacedStructure structure = state.find(action.structureId()).orElse(null);
        if (structure == null || structure.type() != action.type()) {
            pending = null;
            return UndoResult.STRUCTURE_CHANGED;
        }
        if (!matchesExpectedTransform(structure, action)) {
            pending = null;
            return UndoResult.STRUCTURE_CHANGED;
        }

        if (action.kind() == Kind.PLACEMENT) {
            if (structure.isOpen() || structure.storedUnits() > 0 || !structure.itemStorage().isEmpty()) {
                pending = null;
                return UndoResult.STRUCTURE_CHANGED;
            }
            List<SaveGame.ItemStackData> refund = fullRecipeRefund(structure.type());
            if (!inventory.canAccept(refund)) return UndoResult.INVENTORY_FULL;
            if (!state.remove(structure.id(), null)) {
                pending = null;
                return UndoResult.STRUCTURE_CHANGED;
            }
            for (SaveGame.ItemStackData stack : refund) {
                ItemId.parse(stack.itemId()).ifPresent(item -> inventory.add(item, stack.amount()));
            }
            pending = null;
            return UndoResult.PLACEMENT_REVERTED;
        }

        if (!validator.canRestore(structure, action.fromX(), action.fromZ(), action.fromHeading())) {
            return UndoResult.RESTORE_BLOCKED;
        }
        if (!state.relocate(structure.id(), action.fromX(), action.fromZ(), action.fromHeading())) {
            pending = null;
            return UndoResult.STRUCTURE_CHANGED;
        }
        pending = null;
        return UndoResult.RELOCATION_REVERTED;
    }

    void clear() {
        pending = null;
    }

    private static List<SaveGame.ItemStackData> fullRecipeRefund(HomesteadStructureType type) {
        List<SaveGame.ItemStackData> result = new ArrayList<>();
        for (Map.Entry<ItemId, Integer> entry : type.buildCost().entrySet()) {
            if (entry.getValue() > 0) {
                result.add(new SaveGame.ItemStackData(entry.getKey().name(), entry.getValue()));
            }
        }
        return List.copyOf(result);
    }

    private static boolean matchesExpectedTransform(PlacedStructure structure, Action action) {
        return near(structure.x(), action.expectedX())
            && near(structure.z(), action.expectedZ())
            && nearAngle(structure.heading(), action.expectedHeading());
    }

    private static boolean near(float left, float right) {
        return Math.abs(left - right) <= 0.001f;
    }

    private static boolean nearAngle(float left, float right) {
        float difference = Math.abs(normalizeHeading(left) - normalizeHeading(right));
        return Math.min(difference, 360f - difference) <= 0.001f;
    }

    private static float finiteOrZero(float value) {
        return Float.isFinite(value) ? value : 0f;
    }

    private static float normalizeHeading(float value) {
        if (!Float.isFinite(value)) return 0f;
        float normalized = value % 360f;
        return normalized < 0f ? normalized + 360f : normalized;
    }

    enum Kind {
        PLACEMENT,
        RELOCATION
    }

    enum UndoResult {
        PLACEMENT_REVERTED,
        RELOCATION_REVERTED,
        NOTHING_TO_UNDO,
        STRUCTURE_CHANGED,
        INVENTORY_FULL,
        RESTORE_BLOCKED
    }

    @FunctionalInterface
    interface RelocationValidator {
        boolean canRestore(PlacedStructure structure, float x, float z, float heading);
    }

    private record Action(
        Kind kind,
        UUID structureId,
        HomesteadStructureType type,
        float fromX,
        float fromZ,
        float fromHeading,
        float expectedX,
        float expectedZ,
        float expectedHeading
    ) {
        private Action {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(structureId, "structureId");
            Objects.requireNonNull(type, "type");
        }
    }
}
