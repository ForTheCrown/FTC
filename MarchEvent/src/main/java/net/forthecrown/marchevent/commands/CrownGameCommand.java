package net.forthecrown.marchevent.commands;

import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.marchevent.EventMain;
import net.forthecrown.marchevent.PvPEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrownGameCommand extends CrownCommand implements TabCompleter {

    public static boolean prepared = false;
    public static boolean started = false;

    public static void reset(){
        prepared = false;
        started = false;
    }

    public CrownGameCommand(){
        super("crowngame", EventMain.getInstance());

        setPermission("marchevent.crowngame");
        setUsage("&7Usage: &r/gamestart <start | prepare | end | reset>");
        setTabCompleter(this);
        register();
    }

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) throws CrownException {
        if(args.length < 1) return false;

        if(sender instanceof Player){
            Player p = (Player) sender;
            if(!PvPEvent.ARENA_VICINITY.contains(p.getLocation())) throw new InvalidCommandExecution(sender, "&7You need to be at the arena to use this command!");
        }

        PvPEvent event = EventMain.getEvent();

        switch (args[0]){
            case "prepare":
                if(started){
                    sender.sendMessage("Game is already started");
                    return true;
                }
                if(prepared){
                    sender.sendMessage("Game is already prepared");
                    return true;
                }
                if(!event.checkAllPlayersForItems()) return true;

                prepared = true;
                event.moveToStartingPositions();
                sender.sendMessage("Moving teams to starting positions");
                break;

            case "start":
                if(!prepared){
                    sender.sendMessage("Game is not prepared");
                    return true;
                }
                if(started){
                    sender.sendMessage("Game is already started");
                    return true;
                }

                event.startEvent();
                sender.sendMessage("Starting event");
                break;

            case "end":
                if(args.length >= 2){
                    if(args[1].contains("blue")) event.endEvent("&bBlue");
                    else event.endEvent("&eYellow");
                } else event.endEvent();
                sender.sendMessage("Ending event");
                break;

            case "reset":
                event.resetEvent(true);
                sender.sendMessage("Resetting event arena");
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return StringUtil.copyPartialMatches(args[args.length-1], Arrays.asList("prepare", "start", "end", "reset"), new ArrayList<>());
    }
}
