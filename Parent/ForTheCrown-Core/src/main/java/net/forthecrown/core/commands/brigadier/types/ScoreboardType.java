package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardType {
    private static final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    public static ArgumentScoreboardObjective objective(){
        return ArgumentScoreboardObjective.a();
    }

    public static ArgumentScoreboardCriteria criteria(){
        return ArgumentScoreboardCriteria.a();
    }

    public static ArgumentScoreboardTeam team(){
        return ArgumentScoreboardTeam.a();
    }

    public static ArgumentScoreboardSlot slot(){
        return ArgumentScoreboardSlot.a();
    }

    public static Objective getObjective(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return scoreboard.getObjective(ArgumentScoreboardObjective.a(c, argument).getName());
    }

    public static String getCriteria(CommandContext<CommandListenerWrapper> c, String argument){
        return ArgumentScoreboardCriteria.a(c, argument).getName();
    }

    public static Team getTeam(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return scoreboard.getTeam(ArgumentScoreboardTeam.a(c, argument).getName());
    }

    public static int getSlot(CommandContext<CommandListenerWrapper> c, String argument){
        return ArgumentScoreboardSlot.a(c, argument);
    }
}
