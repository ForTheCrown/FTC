package net.forthecrown.core.clickevent;

import org.bukkit.entity.Player;

public interface ClickEventTask {

    void run(Player player, String[] args);
}
