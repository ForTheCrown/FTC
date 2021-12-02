package net.forthecrown.inventory.builder.options;

public interface InventoryOption extends InventoryRunnable, InventoryPlacer {
    int getSlot();

    default OptionPriority getPriority(){
        return OptionPriority.MID;
    }
}
