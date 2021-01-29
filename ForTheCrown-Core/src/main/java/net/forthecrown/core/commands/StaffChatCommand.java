package net.forthecrown.core.commands;

import net.forthecrown.core.CrownCommandExecutor;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CrownCommandExecutor {

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Sends a message to the staff chat
     *
     *
     * Valid usages of command:
     * - /staffchat
     * - /sc
     *
     * Permissions used:
     * - ftc.staffchat
     *
     * Referenced other classes:
     * - Chat: Chat.replaceEmojis
     *
     * Author: Botul
     */

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        String staffPrefix = ChatColor.DARK_GRAY + "[Staff] ";
        String message = staffPrefix + ChatColor.GRAY + "%SENDER%"  + ChatColor.GRAY + ChatColor.BOLD + " > " + ChatColor.RESET + "%MESSAGE%";

        if(args.length < 1) return false;

        String initialMmg = String.join(" ", args);

        initialMmg = CrownUtils.translateHexCodes(FtcCore.replaceEmojis(initialMmg).replace("\\", "\\\\"));
        message = message.replaceAll("%MESSAGE%", initialMmg);

        if(sender instanceof Player) message = message.replaceAll("%SENDER%", sender.getName());
        else message = message.replaceAll("%SENDER%", "Console");

        for (Player p : Bukkit.getOnlinePlayers()){ if(p.hasPermission("ftc.staffchat")) p.sendMessage(message); }
        return true;
    }
}