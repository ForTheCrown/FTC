package net.forthecrown.core.inventory.builder;

public interface InventoryOption extends InventoryRunnable, InventoryPlacer {
    int getSlot();
}
