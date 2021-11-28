package net.forthecrown.poshd.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.crown.EventTimer;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.poshd.Main;
import net.forthecrown.poshd.Messages;
import org.bukkit.entity.Player;

public class CommandStartTimer extends AbstractCommand {

    public CommandStartTimer() {
        super("StartTimer", Main.inst);

        setPermission("ftc.commands.starttimer");
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

            EventTimer timer = new EventTimer(player, Messages.timerFormatter(), plr -> {});
            timer.start(maxMins * 60 * 20);

            Main.TIMERS.put(player.getUniqueId(), timer);

            player.sendMessage(Messages.timerStart());
            player.teleport(PositionArgument.getLocation(c, "destination"));
            Main.logger.info(player.getName() + " entered event");
            return 0;
        };
    }
}