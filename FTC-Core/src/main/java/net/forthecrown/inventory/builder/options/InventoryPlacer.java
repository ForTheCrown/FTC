package net.forthecrown.inventory.builder.options;

import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.user.CrownUser;

public interface InventoryPlacer {
    void place(FtcInventory inventory, CrownUser user);
}
