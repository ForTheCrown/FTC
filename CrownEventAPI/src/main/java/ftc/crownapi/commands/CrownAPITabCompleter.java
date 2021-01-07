package ftc.crownapi.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CrownAPITabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        int argN = args.length -1;
        List<String> argList = null;

        if(args.length == 1){
            argList = new ArrayList<>();
            argList.add("settings");
            argList.add("modifiers");
            argList.add("reload");
        }
        if(args.length == 2){
            argList = new ArrayList<>();
            switch (args[0]){
                case "settings":
                    argList.add("list");
                    argList.add("set");
                    break;
                case "modifiers":
                    argList.add("lobby-location");
                    argList.add("start-location");
                    argList.add("list");
                    break;
                default:
                    return null;
            }
        }

        if(argList == null) return null;
        return StringUtil.copyPartialMatches(args[argN], argList, new ArrayList<>());
    }
}
