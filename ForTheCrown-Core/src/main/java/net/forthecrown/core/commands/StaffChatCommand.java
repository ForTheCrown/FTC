package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.events.ChatEvents;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StaffChatCommand extends CrownCommand implements TabCompleter {

    public StaffChatCommand(){
        super("staffchat", FtcCore.getInstance());

        setPermission("ftc.staffchat");
        setAliases("sc");
        setDescription("Sends a message to the staff chat");
        setUsage("&8Usage: &7/sc <message>");
        setTabCompleter(this);
        register();
    }

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
     * - FtcCore: FtcCore.replaceEmojis
     * - ChatEvents: ChatEvents.sendStaffChatMessage and other variables
     * Author: Botul
     */

    @Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length < 1) return false;

        if(args[0].contains("cmd:")){
            String cmd = args[0].toLowerCase().replaceAll("cmd:", "");

            switch (cmd){
                case "visible":
                    if(ChatEvents.ignoringStaffChat.contains(sender)){
                        ChatEvents.ignoringStaffChat.remove(sender);
                        sender.sendMessage(ChatColor.GRAY + "You will now see staff chat messages again");
                    } else {
                        ChatEvents.ignoringStaffChat.add(sender);
                        sender.sendMessage(ChatColor.GRAY + "You will no longer see staff chat messages");
                    }
                    return true;

                case "mute":
                    if(!sender.hasPermission("ftc.staffchat.admin")) break;
                    if(ChatEvents.scMuted){
                        sender.sendMessage(ChatColor.GRAY + "Staff Chat is no longer muted");
                        ChatEvents.sendStaffChatMessage(null, sender.getName() + " has unmuted staff chat");
                    }
                    else{
                        sender.sendMessage(ChatColor.GRAY + "Staff chat is now muted");
                        ChatEvents.sendStaffChatMessage(null, sender.getName() + " has muted staff chat");
                    }

                    ChatEvents.scMuted = !ChatEvents.scMuted;
                    return true;

                default:
            }
        }
        ChatEvents.sendStaffChatMessage(sender, String.join(" ", args));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        int argN = args.length-1;
        List<String> emojiList = new ArrayList<>();

        if(argN == 0 && args[0].contains("cmd:")){
            emojiList.add("cmd:visible");
            if(sender.hasPermission("ftc.staffchat.admin"))emojiList.add("cmd:mute");
        }

        emojiList.add(":shrug:");
        emojiList.add(":ughcry:");
        emojiList.add(":hug:");
        emojiList.add(":hugcry:");
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

        emojiList.addAll(getPlayerNameList());

        return StringUtil.copyPartialMatches(args[argN], emojiList, new ArrayList<>());
    }
}