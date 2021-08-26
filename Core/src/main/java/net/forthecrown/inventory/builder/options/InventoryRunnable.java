package net.forthecrown.inventory.builder.options;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.user.CrownUser;

public interface InventoryRunnable {
    void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException;
}
