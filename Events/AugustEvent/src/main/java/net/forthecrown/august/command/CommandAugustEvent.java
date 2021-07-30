package net.forthecrown.august.command;

import net.forthecrown.august.AugustPlugin;
import net.forthecrown.august.EventConstants;
import net.forthecrown.august.EventUtil;
import net.forthecrown.august.event.PinataEvent;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CommandAugustEvent extends FtcCommand {

    public CommandAugustEvent() {
        super("august");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     *
     * Permissions used:
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("start")
                        .then(argument("player", EntityArgument.player())
                                .executes(c -> {
                                    CommandSource source = c.getSource();
                                    Player player = EntityArgument.getPlayer(c, "player");

                                    if(PinataEvent.currentEntry != null) throw FtcExceptionProvider.create("There is already someone in the event");

                                    AugustPlugin.event.start(player);

                                    source.sendAdmin("Forcing " + player.getName() + " to enter the event");
                                    return 0;
                                })
                        )
                )

                .then(literal("end")
                        .executes(c -> {
                            if(PinataEvent.currentEntry == null) throw FtcExceptionProvider.create("There is no one in the event");

                            AugustPlugin.event.end(PinataEvent.currentEntry);

                            c.getSource().sendAdmin("Ending event");
                            return 0;
                        })
                )

                .then(literal("complete")
                        .executes(c -> {
                            if(PinataEvent.currentEntry == null) throw FtcExceptionProvider.create("There is no one in the event");

                            AugustPlugin.event.complete(PinataEvent.currentEntry);

                            c.getSource().sendAdmin("Completing event");
                            return 0;
                        })
                )

                .then(literal("give_ticket")
                        .then(argument("player", EntityArgument.player())
                                .executes(c -> {
                                    Player player = EntityArgument.getPlayer(c, "player");

                                    player.getInventory().addItem(EventConstants.ticket());

                                    c.getSource().sendAdmin("Giving ticket");
                                    return 0;
                                })
                        )
                )

                .then(literal("spawn_plus_one")
                        .executes(c -> {
                            Player player = c.getSource().asPlayer();

                            EventUtil.spawnPlusX(player.getLocation().add(1, 1, 1), 1);

                            c.getSource().sendAdmin("Spawning plus one");
                            return 0;
                        })
                )

                .then(literal("spawn_pinata")
                        .then(argument("cords", PositionArgument.position())
                                .executes(c -> {
                                    Location loc = PositionArgument.getLocation(c, "cords");

                                    EventUtil.spawnPinata(loc);

                                    c.getSource().sendAdmin("Spawned pinata");
                                    return 0;
                                })
                        )
                );
    }
}