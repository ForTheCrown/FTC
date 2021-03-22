package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.command.CommandSender;

public class HelpPost extends CrownCommandBuilder {

    public HelpPost(){
        super("posthelp", FtcCore.getInstance());

        setAliases("polehelp");
        setPermission(null);
        setDescription("Displays info for region poles.");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Displays information about region poles.
     *
     *
     * Valid usages of command:
     * - /posthelp
     * - /polehelp
     *
     * Referenced other classes:
     * - FtcCore: FtcCore.getPrefix
     * - Findpole
     *
     * Author: Wout
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c ->{
            CommandSender sender = c.getSource().getBukkitSender();

            // Send information
            sender.sendMessage(FtcCore.getPrefix() + ChatColor.YELLOW + "Information about regionpoles:");
            sender.sendMessage("You can only teleport between regionpoles.");
            sender.sendMessage("Use " + ChatColor.YELLOW + "/findpole" + ChatColor.RESET + " to find the closest pole.");
            sender.sendMessage("Use " + ChatColor.YELLOW + "/visit" + ChatColor.RESET + " to travel between them.");
            sender.sendMessage("Use " + ChatColor.YELLOW + "/movein" + ChatColor.RESET + " to make a pole your home.");
            sender.sendMessage("Then use " + ChatColor.YELLOW + "/home" + ChatColor.RESET + " to go there.");

            return 0;
        });
    }
}
