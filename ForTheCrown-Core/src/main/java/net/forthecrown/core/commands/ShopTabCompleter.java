package net.forthecrown.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = Arrays.asList("drops", "farming", "mining", "web");
        if(args.length == 1) return StringUtil.copyPartialMatches(args[0], argList, new ArrayList<>());

        return null;
    }
}
