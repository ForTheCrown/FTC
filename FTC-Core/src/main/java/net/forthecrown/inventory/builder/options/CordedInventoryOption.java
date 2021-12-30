package net.forthecrown.inventory.builder.options;

import net.forthecrown.inventory.builder.InventoryPos;

/**
 * An {@link InventoryOption} which holds the inventory slot tag in an {@link InventoryPos} instance.
 */
public interface CordedInventoryOption extends InventoryOption {
    @Override
    default int getSlot() {
        return getPos().getSlot();
    }

    InventoryPos getPos();
}
