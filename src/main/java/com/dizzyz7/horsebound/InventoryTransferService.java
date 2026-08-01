// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.util.Objects;

/** Pure-Java exact transfer policy shared by backpack and Chest presentation. */
final class InventoryTransferService {
    TransferResult transfer(
        Inventory source,
        Inventory destination,
        ItemId item,
        int selectedStackAmount,
        TransferMode mode
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(item, "item");
        TransferMode safeMode = mode == null ? TransferMode.ONE : mode;

        int available = source.count(item);
        if (available <= 0) return new TransferResult(TransferStatus.NO_ITEM, 0);

        int requested = switch (safeMode) {
            case ONE -> 1;
            case STACK -> Math.max(1, Math.min(available, selectedStackAmount));
            case ALL -> available;
        };
        if (destination.availableSpace(item) < requested) {
            return new TransferResult(TransferStatus.FULL, 0);
        }
        if (!source.transferTo(destination, item, requested)) {
            return new TransferResult(TransferStatus.FULL, 0);
        }
        return new TransferResult(TransferStatus.SUCCESS, requested);
    }

    enum TransferMode {
        ONE,
        STACK,
        ALL
    }

    enum TransferStatus {
        SUCCESS,
        FULL,
        NO_ITEM
    }

    record TransferResult(TransferStatus status, int moved) {
        TransferResult {
            status = Objects.requireNonNull(status, "status");
            moved = Math.max(0, moved);
            if (status != TransferStatus.SUCCESS) moved = 0;
        }
    }
}
