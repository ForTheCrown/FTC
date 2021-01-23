package net.forthecrown.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ShopEditTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        int argN = args.length-1;
        List<String> toReturn = new ArrayList<>();

        if(args.length == 1){
            toReturn.add("line1");
            toReturn.add("line2");
            toReturn.add("price");
            return StringUtil.copyPartialMatches(args[argN], toReturn, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
