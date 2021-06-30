package net.forthecrown.core.inventory.builder;

import net.forthecrown.user.CrownUser;
import org.bukkit.inventory.Inventory;

public interface InventoryPlacer {
    void place(Inventory inventory, CrownUser user);
}
