package net.forthecrown.core.inventory.builder;

import net.forthecrown.user.CrownUser;

public interface InventoryAction {
    void run(CrownUser user);
}
