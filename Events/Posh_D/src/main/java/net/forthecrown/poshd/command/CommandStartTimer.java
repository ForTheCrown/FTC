package net.forthecrown.poshd.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.crown.EventTimer;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.poshd.EventUtil;
import net.forthecrown.poshd.Main;
import net.forthecrown.poshd.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
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
                                .then(argument("exit", PositionArgument.position())
                                        .executes(startTimer(false))

                                        .then(argument("maxMins", IntegerArgumentType.integer(0, 100))
                                                .executes(startTimer(true))
                                        )
                                )
                        )
                );
    }

    public static final TranslatableExceptionType INV_NOT_EMPTY = new TranslatableExceptionType("error.itemsInInv");

    Command<CommandSource> startTimer(boolean maxMinsGiven) {
        return c -> {
            Player player = EntityArgument.getPlayer(c, "player");
            int maxMins = maxMinsGiven ? c.getArgument("maxMins", Integer.class) : 5;
            Location loc = PositionArgument.getLocation(c, "destination");
            Location exit = PositionArgument.getLocation(c, "exit");

            // Move them away if their inventory is empty
            if(!player.getInventory().isEmpty()) {
                player.teleport(exit);
                // Can't be bothered to figure out how to display that INV_NOT_EMPTY msg to the player in a cool way
                // so I just stole this from way of doing it from Messages
                player.sendMessage(Component.translatable("error.itemsInInv"));
                throw INV_NOT_EMPTY.create();
            }

            // Create timer
            EventTimer timer = EventUtil.createTimer(player,    plr -> EventUtil.leave(plr, exit));
            timer.checkPoint = loc;
            timer.exitLocation = exit;
            timer.start(maxMins * 60 * 20);

            player.sendMessage(Messages.timerStart());
            player.teleport(loc);
            Main.logger.info(player.getName() + " entered event");
            return 0;
        };
    }
}
