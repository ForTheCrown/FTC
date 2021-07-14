package net.forthecrown.inventory.builder.options;

import net.forthecrown.inventory.builder.InvCords;

/**
 * An {@link InventoryOption} which holds the inventory slot data in an {@link InvCords} instance.
 */
public interface CordedInventoryOption extends InventoryOption {
    @Override
    default int getSlot() {
        return getCoordinates().getSlot();
    }

    InvCords getCoordinates();
}
