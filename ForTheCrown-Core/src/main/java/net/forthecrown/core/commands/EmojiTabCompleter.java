package net.forthecrown.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class EmojiTabCompleter implements TabCompleter {

    /*
     * Tabcompleter for emojis in /sc
     * Author: Botul
     */

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
