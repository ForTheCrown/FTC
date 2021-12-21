package net.forthcrown.poshd.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthcrown.crown.EventTimer;
import net.forthcrown.poshd.Main;
import net.forthcrown.poshd.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CommandStartTimer extends AbstractCommand {

    public CommandStartTimer() {
        super("StartTimer", Main.inst);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /StartTimer <player> [max minutes in event]
     *
     * Permissions used:
     * ftc.commands.starttimer
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("player", EntityArgument.player())
                        .then(argument("destination", PositionArgument.position())
                                .executes(startTimer(false))

                                .then(argument("maxMins", IntegerArgumentType.integer(0, 100))
                                        .executes(startTimer(true))
                                )
                        )
                );
    }

    Command<CommandSource> startTimer(boolean maxMinsGiven) {
        return c -> {
            Player player = EntityArgument.getPlayer(c, "player");
            int maxMins = maxMinsGiven ? c.getArgument("maxMins", Integer.class) : 5;
            Location loc = PositionArgument.getLocation(c, "destination");

            EventTimer timer = Main.createTimer(player, plr -> plr.teleport(loc));
            timer.start(maxMins * 60 * 20);

            player.sendMessage(Messages.timerStart());
            player.teleport(PositionArgument.getLocation(c, "destination"));
            Main.logger.info(player.getName() + " entered event");
            return 0;
        };
    }
}
