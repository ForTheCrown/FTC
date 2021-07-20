package net.forthecrown.august.command;

import net.forthecrown.august.A_Main;
import net.forthecrown.august.AugustEvent;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
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

                                    if(AugustEvent.currentEntry != null) throw FtcExceptionProvider.create("There is already someone in the event");

                                    A_Main.event.start(player);

                                    source.sendAdmin("Forcing " + player.getName() + " to enter the event");
                                    return 0;
                                })
                        )
                )

                .then(literal("end")
                        .then(argument("player", EntityArgument.player())
                                .executes(c -> {

                                })
                        )
                );
    }
}