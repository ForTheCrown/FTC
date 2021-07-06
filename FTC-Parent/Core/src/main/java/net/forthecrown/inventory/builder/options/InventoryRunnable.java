package net.forthecrown.inventory.builder.options;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.user.CrownUser;

public interface InventoryRunnable {
    void onClick(CrownUser user, ClickContext context) throws RoyalCommandException;
}
