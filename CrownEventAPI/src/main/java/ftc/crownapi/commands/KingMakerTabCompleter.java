package ftc.crownapi.commands;

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
        if(args.length == 1){
            for(Player p: Bukkit.getOnlinePlayers()){
                argList.add(p.getName());
            }
            argList.add("remove");
            return StringUtil.copyPartialMatches(args[0], argList, new ArrayList<>());
        }
        if(args.length == 2){
            argList = new ArrayList<>();
            argList.add("king");
            argList.add("queen");
            return StringUtil.copyPartialMatches(args[1], argList, new ArrayList<>());
        }
        return null;
    }
}
