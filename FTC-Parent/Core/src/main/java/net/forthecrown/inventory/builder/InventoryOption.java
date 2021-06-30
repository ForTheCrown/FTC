package net.forthecrown.inventory.builder;

public interface InventoryOption extends InventoryRunnable, InventoryPlacer {
    int getSlot();
}
