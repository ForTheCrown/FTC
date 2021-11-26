package net.forthecrown.poshd.command;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.crownevents.CrownEventUtils;
import net.forthecrown.crownevents.EventTimer;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.poshd.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class CommandStopTimer extends FtcCommand {

    public CommandStopTimer() {
        super("StopTimer");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /StopTimer <player> <scoreboard objective> <x y z>
     *
     * Permissions used:
     * ftc.commands.stoptimer
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("player", EntityArgument.player())
                        .then(argument("scoreboard", ObjectiveArgument.objective())
                                .then(argument("destination", PositionArgument.position())
                                        .executes(c -> {
                                            Player player = EntityArgument.getPlayer(c, "player");
                                            Objective objective = ObjectiveArgument.getObjective(c, "scoreboard");
                                            Location destination = PositionArgument.getLocation(c, "destination");

                                            EventTimer timer = Main.TIMERS.get(player.getUniqueId());
                                            if(timer != null) {
                                                if(!timer.wasStopped()) timer.stop();
                                                Main.TIMERS.remove(player.getUniqueId());

                                                int scoreVal = (int) timer.getTime();
                                                Score score = objective.getScore(player.getName());

                                                Component message;

                                                if(CrownEventUtils.isNewRecord(score, scoreVal)) {
                                                    score.setScore(scoreVal);

                                                    message = Component.text("New record! ");
                                                } else {
                                                    message = Component.text("Better luck next time :D ");
                                                }

                                                player.sendMessage(
                                                        message.append(Component.text(EventTimer.getTimerCounter(timer.getTime()).toString()))
                                                );
                                            } else Main.logger.warning(player.getName() + " did not have EventTimer");

                                            Main.logger.info(player.getName() + " left " + objective.getName());
                                            player.teleport(destination);
                                            return 0;
                                        })
                                )
                        )
                );
    }
}