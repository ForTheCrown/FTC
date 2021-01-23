package net.forthecrown.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class KingMakerTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = new ArrayList<>();
        int argN = args.length -1;

        if(args.length == 1){
            argList.add("remove");
            for (Player p : Bukkit.getOnlinePlayers()){
                argList.add(p.getName());
            }
            return StringUtil.copyPartialMatches(args[argN], argList, new ArrayList<>());
        }
        if(args.length == 2){
            argList.add("queen");
            argList.add("king");
        }
        return StringUtil.copyPartialMatches(args[argN], argList, new ArrayList<>());
    }
}
