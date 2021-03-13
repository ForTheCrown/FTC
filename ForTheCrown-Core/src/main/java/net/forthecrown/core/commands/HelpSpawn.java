package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
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
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c ->{
            CommandSender sender = c.getSource().getBukkitSender();

            // Information:
            sender.sendMessage(FtcCore.getPrefix() + ChatColor.YELLOW + "Information about spawn:");
            sender.sendMessage("Spawn is called Hazelguard, you can tp using regionpoles.");
            sender.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
            sender.sendMessage("Then, use " + ChatColor.YELLOW + "/visit Hazelguard" + ChatColor.RESET + " to travel to spawn.");
            sender.sendMessage(ChatColor.GRAY + "If you need more help, use /posthelp.");

            return 0;
        });
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        // Information:
        sender.sendMessage(FtcCore.getPrefix() + ChatColor.YELLOW + "Information about spawn:");
        sender.sendMessage("Spawn is called Hazelguard, you can tp using regionpoles.");
        sender.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
        sender.sendMessage("Then, use " + ChatColor.YELLOW + "/visit Hazelguard" + ChatColor.RESET + " to travel to spawn.");
        sender.sendMessage(ChatColor.GRAY + "If you need more help, use /posthelp.");

        return true;
    }*/
}
