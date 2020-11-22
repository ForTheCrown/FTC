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
            argsList.add("spawnpresent");
            argsList.add("reload");
            argsList.add("setchallenge");
            argsList.add("usechallenge");
            argsList.add("stoploop");
            argsList.add("startloop");
            return StringUtil.copyPartialMatches(args[argN], argsList, new ArrayList<>());
        }
        return null;
    }
}
