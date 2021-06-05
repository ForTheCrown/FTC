package net.forthecrown.emperor.clickevent;

import net.forthecrown.emperor.CrownException;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import org.bukkit.entity.Player;

public interface ClickEventTask {
    /**
     * The piece of code ran when a specific click event is called
     * @param player The player that called the click event
     * @param args Any extra args you may want to pass in
     */
    void run(Player player, String[] args) throws CrownException, RoyalCommandException;
}
