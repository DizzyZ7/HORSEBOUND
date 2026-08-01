// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryTransferServiceTest {
    private final InventoryTransferService service = new InventoryTransferService();

    @Test
    void oneStackAndAllUseExactAtomicAmounts() {
        Inventory source = new Inventory(24);
        Inventory destination = new Inventory(12);
        source.set(ItemId.WOOD, 150);

        InventoryTransferService.TransferResult one = service.transfer(
            source, destination, ItemId.WOOD, 99, InventoryTransferService.TransferMode.ONE
        );
        InventoryTransferService.TransferResult stack = service.transfer(
            source, destination, ItemId.WOOD, 50, InventoryTransferService.TransferMode.STACK
        );
        InventoryTransferService.TransferResult all = service.transfer(
            source, destination, ItemId.WOOD, 99, InventoryTransferService.TransferMode.ALL
        );

        assertEquals(InventoryTransferService.TransferStatus.SUCCESS, one.status());
        assertEquals(1, one.moved());
        assertEquals(50, stack.moved());
        assertEquals(99, all.moved());
        assertEquals(0, source.count(ItemId.WOOD));
        assertEquals(150, destination.count(ItemId.WOOD));
    }

    @Test
    void insufficientDestinationSpaceDoesNotPartiallyMoveStack() {
        Inventory source = new Inventory(24);
        Inventory destination = new Inventory(1);
        source.set(ItemId.WOOD, 50);
        destination.set(ItemId.WOOD, 90);

        InventoryTransferService.TransferResult result = service.transfer(
            source, destination, ItemId.WOOD, 50, InventoryTransferService.TransferMode.STACK
        );

        assertEquals(InventoryTransferService.TransferStatus.FULL, result.status());
        assertEquals(0, result.moved());
        assertEquals(50, source.count(ItemId.WOOD));
        assertEquals(90, destination.count(ItemId.WOOD));
    }

    @Test
    void allTransferRequiresSpaceForEntireItemTotal() {
        Inventory source = new Inventory(24);
        Inventory destination = new Inventory(1);
        source.set(ItemId.HAY, 30);
        destination.set(ItemId.HAY, 80);

        InventoryTransferService.TransferResult result = service.transfer(
            source, destination, ItemId.HAY, 20, InventoryTransferService.TransferMode.ALL
        );

        assertEquals(InventoryTransferService.TransferStatus.FULL, result.status());
        assertEquals(30, source.count(ItemId.HAY));
        assertEquals(80, destination.count(ItemId.HAY));
    }
}
