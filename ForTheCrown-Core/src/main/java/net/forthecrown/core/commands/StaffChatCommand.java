package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class StaffChatCommand implements CommandExecutor, TabCompleter {

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String staffPrefix = ChatColor.DARK_GRAY + "[Staff] ";
        String message = staffPrefix + ChatColor.GRAY + "%SENDER%"  + ChatColor.GRAY + ChatColor.BOLD + " >" + ChatColor.RESET + " ";

        if(args.length < 1) return false;

        String initialMmg = String.join(" ", args);

        initialMmg = FtcCore.translateHexCodes(FtcCore.replaceEmojis(initialMmg).replace("\\", "\\\\"));

        if(sender instanceof Player) message = message.replaceAll("%SENDER%", sender.getName());
        else message = message.replaceAll("%SENDER%", "Console");

        for (Player p : Bukkit.getOnlinePlayers()){ if(p.hasPermission("ftc.staffchat")) p.sendMessage(message + initialMmg); }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        int argN = args.length-1;

        List<String> emojiList = new ArrayList<>();
        emojiList.add(":shrug:");
        emojiList.add(":ughcry:");
        emojiList.add(":gimme:");
        emojiList.add(":gimmecry:");
        emojiList.add(":bear:");
        emojiList.add(":smooch:");
        emojiList.add(":why:");
        emojiList.add(":tableflip:");
        emojiList.add(":tableput:");
        emojiList.add(":pretty:");
        emojiList.add(":sparkle:");
        emojiList.add(":blush:");
        emojiList.add(":sad:");
        emojiList.add(":pleased:");
        emojiList.add(":fedup:");

        return StringUtil.copyPartialMatches(args[argN], emojiList, new ArrayList<>());
    }
}