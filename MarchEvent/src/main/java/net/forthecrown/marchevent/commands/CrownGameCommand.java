package net.forthecrown.marchevent.commands;

import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.marchevent.PvPEvent;
import net.forthecrown.marchevent.EventMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrownGameCommand extends CrownCommand implements TabCompleter {

    public CrownGameCommand(){
        super("crowngame", EventMain.getInstance());

        setPermission("marchevent.crowngame");
        setUsage("&7Usage: &r/gamestart <start | prepare | end | reset>");
        register();
    }

    @Override
    public boolean run(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) throws CrownException {
        if(args.length < 1) return false;

        PvPEvent event = EventMain.getEvent();

        switch (args[0]){
            case "prepare":
                if(!event.checkAllPlayersForItems()) return true;

                event.moveToStartingPositions();
                sender.sendMessage("Moving teams to starting positions");
                break;

            case "start":
                event.startEvent();
                sender.sendMessage("Starting event");
                break;

            case "end":
                if(args.length < 2){
                    if(args[1].contains("blue")) event.endEvent("&bBlue");
                    else event.endEvent("&eYellow");
                } else event.endEvent();
                sender.sendMessage("Ending event");
                break;

            case "reset":
                event.resetEvent();
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
