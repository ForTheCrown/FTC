package net.forthecrown.vikings.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.InvalidArgumentException;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.blessings.VikingBlessing;
import net.forthecrown.vikings.raids.RaidDifficulty;
import net.forthecrown.vikings.raids.RaidHandler;
import net.forthecrown.vikings.raids.VikingRaid;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VikingsCommand extends CrownCommand implements TabCompleter {

    public VikingsCommand() {
        super("viking", Vikings.getInstance());

        setPermission("ftc.vikings.admin");
        setTabCompleter(this);
        setUsage("&7Usage: /vikings <test> <raid name>\n /viking <end | complete>");
        register();
    }

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) throws CrownException {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);
        if(args.length < 1) return false;

        Player player = (Player) sender;

        if(args[0].contains("test")){
            if(args.length < 2) return false;

            VikingRaid raid = RaidHandler.fromName(args[1]);
            if(raid == null) throw new InvalidArgumentException(sender, args[1] + " is not a valid raid");

            RaidDifficulty difficulty = RaidDifficulty.NORMAL;
            if(args.length == 3){
                try {
                    difficulty = RaidDifficulty.valueOf(args[2].toUpperCase());
                } catch (Exception ignored) {}
            }

            Vikings.getRaidHandler().callRaid(raid, player, difficulty);
        }
        else if(args[0].contains("end")){
            VikingRaid raid = RaidHandler.fromPlayer(player);
            if(raid == null) return false;

            raid.onRaidEnd();
        }
        else if(args[0].contains("complete")){
            VikingRaid raid = RaidHandler.fromPlayer(player);
            if(raid == null) return false;

            raid.onRaidComplete();
        }
        else if (args[0].contains("blessing")){
            if(args.length != 3) return false;

            VikingBlessing blessing = VikingBlessing.fromName(args[2]);
            if(blessing == null) throw new InvalidArgumentException(sender, args[2] + " is not a valid blessing");
            CrownUser user = FtcCore.getUser(player);

            if(args[1].contains("use")){
                blessing.beginUsage(user);
                player.sendMessage("Starting blessing usage!");
            }

            if(args[1].contains("stop")){
                blessing.endUsage(user);
                user.sendMessage("You are no longer using a blessing");
            }
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
            toReturn.add("blessing");
        }

        if(args.length == 2 && args[0].contains("test")){
            for (VikingRaid r : Vikings.getRaidHandler().getRaids()){
                toReturn.add(r.getName());
            }
        }

        if(args.length == 3 && args[0].contains("test")){
            for (RaidDifficulty d: RaidDifficulty.values()){
                toReturn.add(d.toString());
            }
        }

        if(args.length == 2 && args[0].contains("blessing")){
            toReturn.add("use");
            toReturn.add("stop");
        }

        if(args.length == 3 && (args[1].contains("use") || args[1].contains("stop"))){
            for (VikingBlessing b : VikingBlessing.getBlessings()){
                toReturn.add(b.getName());
            }
        }

        return toReturn;
    }
}
