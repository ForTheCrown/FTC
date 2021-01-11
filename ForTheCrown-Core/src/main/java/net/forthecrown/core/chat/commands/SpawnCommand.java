package net.forthecrown.core.chat.commands;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SpawnCommand implements CommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Explains how to get to spawn.
     *
     *
     * Valid usages of command:
     * - /spawn
     *
     * Referenced other classes:
     * - FtcCore
     * - Findpost
     * - Posthelp
     *
     * Author: Wout
     */

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Information:
        sender.sendMessage(FtcCore.getPrefix() + ChatColor.YELLOW + "Information about spawn:");
        sender.sendMessage("Spawn is called Hazelguard, you can tp using regionpoles.");
        sender.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
        sender.sendMessage("Then, use " + ChatColor.YELLOW + "/visit Hazelguard" + ChatColor.RESET + " to travel to spawn.");
        sender.sendMessage(ChatColor.GRAY + "If you need more help, use /posthelp.");

        return true;
    }
}
