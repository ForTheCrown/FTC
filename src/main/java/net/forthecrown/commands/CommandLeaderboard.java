package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import static org.bukkit.Bukkit.getServer;

public class CommandLeaderboard extends FtcCommand {

    private final String objectiveName;
    private CommandLeaderboard(String objectiveName) {
        super(objectiveName + "top");
        this.objectiveName = objectiveName;

        setPermission(Permissions.DEFAULT);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Leaderboard
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player player = c.getSource().asPlayer();

                    Scoreboard mainScoreboard = getServer().getScoreboardManager().getMainScoreboard();
                    Objective objective = mainScoreboard.getObjective(objectiveName);

                    TextComponent displayName = Component.text()
                            .color(NamedTextColor.GOLD)
                            .append(Component.text("---"))
                            .append(Component.text("Leaderboard").color(NamedTextColor.YELLOW))
                            .append(Component.text("---"))
                            .build();

                    Scoreboard scoreboard = getServer().getScoreboardManager().getNewScoreboard();
                    Objective newObj = scoreboard.registerNewObjective(player.getName(), "dummy", displayName);

                    for(String name : objective.getScoreboard().getEntries()) {
                        //If you don't have a set score, or your score is 0, dont' show it
                        if(!objective.getScore(name).isScoreSet() || objective.getScore(name).getScore() == 0) continue;

                        newObj.getScore(name).setScore(objective.getScore(name).getScore());
                    }

                    newObj.setDisplaySlot(DisplaySlot.SIDEBAR);
                    player.setScoreboard(scoreboard);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(FTC.getPlugin(), () -> player.setScoreboard(mainScoreboard), 15 * 20);

                    return 0;
                });
    }

    public static void createCommands() {
        new CommandLeaderboard("Death");

        new CommandLeaderboard("crown") {
            @Override
            public boolean test(CommandSource source) {
                if(!GeneralConfig.crownEventActive) {
                    return false;
                }

                return super.test(source);
            }
        };
    }
}