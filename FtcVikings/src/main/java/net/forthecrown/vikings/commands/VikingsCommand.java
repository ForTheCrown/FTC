package net.forthecrown.vikings.commands;

import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.raids.managers.RaidDifficulty;
import net.forthecrown.vikings.raids.managers.VikingRaid;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class VikingsCommand extends CrownCommand implements TabCompleter {

    public VikingsCommand() {
        super("viking", Vikings.getInstance());

        setPermission("ftc.vikings.admin");
        setTabCompleter(this);
        setUsage("&7Usage: /vikings <test> <raid name>\nor /viking <end | complete>");
        register();
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) throws CrownException {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        if(args.length < 1) return false;

        if(args[0].contains("test")){
            if(args.length < 2) return false;

            VikingRaid raid = Vikings.getRaidHandler().getFromName(args[1]);
            if(raid == null) throw new InvalidArgumentException(sender, args[1] + " is not a valid raid");

            RaidDifficulty difficulty = RaidDifficulty.NORMAL;
            if(args.length == 3){
                try {
                    difficulty = RaidDifficulty.valueOf(args[2].toUpperCase());
                } catch (Exception ignored) {}
            }

            Vikings.getRaidHandler().callRaid(raid, (Player) sender, difficulty);
        }
        else if(args[0].contains("end")){
            VikingRaid raid = Vikings.getRaidHandler().getFromPlayer(((Player) sender));
            if(raid == null) return false;

            raid.onRaidEnd();
        }
        else if(args[0].contains("complete")){
            VikingRaid raid = Vikings.getRaidHandler().getFromPlayer(((Player) sender));
            raid.onRaidComplete();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> toReturn = new ArrayList<>();

        if(args.length == 1){
            toReturn.add("test");
            toReturn.add("end");
            toReturn.add("complete");
        }

        if(args.length == 2 && args[0].contains("test")){
            for (VikingRaid r : Vikings.getRaidHandler().getRaids()){
                toReturn.add(r.getName());
            }
        }

        return StringUtil.copyPartialMatches(args[args.length-1], toReturn, new ArrayList<>());
    }
}
