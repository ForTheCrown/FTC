package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpSpawn extends CrownCommandBuilder {

    public HelpSpawn(){
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
    protected void createCommand(BrigadierCommand command) {
        command.executes(c ->{
            CommandSender sender = c.getSource().asBukkit();

            // Information:
            sender.sendMessage(FtcCore.getPrefix() + ChatColor.YELLOW + "Information about spawn:");
            sender.sendMessage("Spawn is called Hazelguard, you can tp using regionpoles.");
            sender.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
            sender.sendMessage("Then, use " + ChatColor.YELLOW + "/visit Hazelguard" + ChatColor.RESET + " to travel to spawn.");
            sender.sendMessage(ChatColor.GRAY + "If you need more help, use /posthelp.");

            return 0;
        });
    }
}
