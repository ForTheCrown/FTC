package net.forthecrown.poshd.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.crownevents.EventTimer;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.poshd.Main;
import org.bukkit.entity.Player;

public class CommandStartTimer extends FtcCommand {

    public CommandStartTimer() {
        super("StartTimer");

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

            EventTimer timer = new EventTimer(player, plr -> {});
            timer.start(maxMins * 60 * 20);

            player.teleport(PositionArgument.getLocation(c, "destination"));
            Main.logger.info(player.getName() + " entered event");
            return 0;
        };
    }
}