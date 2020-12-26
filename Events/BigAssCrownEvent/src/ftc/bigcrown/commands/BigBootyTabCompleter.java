package ftc.bigcrown.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class BigBootyTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        int argN = args.length - 1;
        if(args.length == 1){
            List<String> argsList = new ArrayList<>();
            argsList.add("setloc");
            argsList.add("reload");
            argsList.add("usechallenge");
            argsList.add("stoploop");
            argsList.add("startloop");
            return StringUtil.copyPartialMatches(args[argN], argsList, new ArrayList<>());
        }
        if(args[0].contains("usechallenge") && args.length == 2){
            List<String> argsList = new ArrayList<>();
            argsList.add("RACE");
            argsList.add("PROTECT_HAROLD");
            argsList.add("PVE_ARENA");
            argsList.add("PINATA");
            argsList.add("HUNT_BATS");
            argsList.add("ENDERMEN");
            return StringUtil.copyPartialMatches(args[argN], argsList, new ArrayList<>());
        }
        return null;
    }
}
