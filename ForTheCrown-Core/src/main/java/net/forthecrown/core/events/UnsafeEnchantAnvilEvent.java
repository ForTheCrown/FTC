package net.forthecrown.core.events;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.Branch;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class UnsafeEnchantAnvilEvent implements Listener {

    //TODO: everything lol
    @EventHandler(ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        for(HumanEntity entity : event.getViewers()){
            if(!FtcCore.getUser(entity.getUniqueId()).getBranch().equals(Branch.PIRATES)) return;
        }
    }
}
