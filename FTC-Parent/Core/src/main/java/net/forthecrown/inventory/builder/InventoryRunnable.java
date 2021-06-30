package net.forthecrown.inventory.builder;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.user.CrownUser;

public interface InventoryRunnable {
    void run(CrownUser user, ClickContext context) throws RoyalCommandException;
}
