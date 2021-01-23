package net.forthecrown.core.commands;

import net.forthecrown.core.enums.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CoreTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = new ArrayList<>();
        int argN = args.length -1;

        if(args.length == 1){
            argList.add("reload");
            argList.add("save");
            argList.add("announcer");
            argList.add("user");
        }
        if(args.length == 2){
            switch (args[0]){
                case "reload":
                case "save":
                    argList.add("announcer");
                    argList.add("balances");
                    argList.add("users");
                    argList.add("signshop");
                    break;
                case "announcer":
                    argList.add("stop");
                    argList.add("start");
                    break;
                case "user":
                    return null;
                default:
                    return new ArrayList<>();
            }
        }

        if(args.length == 3 && args[1].contains("user")){
            argList.add("addpet");
            argList.add("rank");
            argList.add("makebaron");
            argList.add("canswapbranch");
            argList.add("branch");
            argList.add("addgems");
        }

        if(args.length == 4){
            switch (args[2]){
                case "rank":
                    argList.add("add");
                    argList.add("remove");
                case "branch":
                    argList.add("DEFAULT");
                    argList.add("VIKINGS");
                    argList.add("PIRATES");
                    argList.add("ROYALS");
                default:
                    return new ArrayList<>();
            }
        }

        if(args.length == 5 && args[3].contains("rank")){
            for(Rank r : Rank.values()){
                argList.add(r.toString());
            }
        }

        return StringUtil.copyPartialMatches(args[argN], argList, new ArrayList<>());
    }
}

// addpet, rank <remove | add | list>, makebaron, canswapbranch [set], branch <get | set>,
