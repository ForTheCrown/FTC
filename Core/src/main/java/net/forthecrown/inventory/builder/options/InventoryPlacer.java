package net.forthecrown.inventory.builder.options;

import net.forthecrown.user.CrownUser;
import org.bukkit.inventory.Inventory;

public interface InventoryPlacer {
    void place(Inventory inventory, CrownUser user);
}
