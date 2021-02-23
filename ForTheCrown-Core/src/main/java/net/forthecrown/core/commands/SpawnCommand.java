package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class SpawnCommand extends CrownCommand {

    public SpawnCommand(){
        super("spawn", FtcCore.getInstance());

        setPermission(null);
        register();
    }

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
     * - FtcCore: FtcCore.getPrefix
     * - Findpost
     * - Posthelp
     *
     * Author: Wout
     */

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        // Information:
        sender.sendMessage(FtcCore.getPrefix() + ChatColor.YELLOW + "Information about spawn:");
        sender.sendMessage("Spawn is called Hazelguard, you can tp using regionpoles.");
        sender.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
        sender.sendMessage("Then, use " + ChatColor.YELLOW + "/visit Hazelguard" + ChatColor.RESET + " to travel to spawn.");
        sender.sendMessage(ChatColor.GRAY + "If you need more help, use /posthelp.");

        return true;
    }
}
