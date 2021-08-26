package net.forthecrown.commands.clickevent;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.entity.Player;

public interface ClickEventTask {
    /**
     * The piece of code ran when a specific click event is called
     * @param player The player that called the click event
     * @param args Any extra args you may want to pass in
     */
    void run(Player player, String[] args) throws CommandSyntaxException;
}
