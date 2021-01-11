package net.forthecrown.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DataTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = new ArrayList<>();
        int argN = args.length -1;

        if(args.length == 1){
            argList.add("reload");
            argList.add("save");
        }
        if(args.length == 2){
            switch (args[1]){
                case "reload":
                case "save":
                    argList.add("announcer");
                    argList.add("balances");
                    argList.add("userdata");
                    argList.add("economy");
                    argList.add("signshop");
                    break;
                default:
                    return null;
            }
        }

        return StringUtil.copyPartialMatches(args[argN], argList, new ArrayList<>());
    }
}
