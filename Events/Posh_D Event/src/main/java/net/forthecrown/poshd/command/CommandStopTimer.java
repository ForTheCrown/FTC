package net.forthecrown.poshd.command;

import net.forthecrown.crown.CrownEventUtils;
import net.forthecrown.crown.EventTimer;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.scoreboard.ObjectiveArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.poshd.Main;
import net.forthecrown.poshd.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class CommandStopTimer extends AbstractCommand {

    public CommandStopTimer() {
        super("StopTimer", Main.inst);

        setPermission("ftc.commands.starttimer");
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

                                                    message = Messages.timerRecord(timer);
                                                } else {
                                                    message = Messages.timerNotNew(timer);
                                                }

                                                player.sendMessage(message);
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