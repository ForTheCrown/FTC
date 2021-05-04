package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.text.Component;

public class CommandBroadcast extends CrownCommandBuilder {

    public CommandBroadcast(){
        super("broadcast", FtcCore.getInstance());

        setDescription("Broadcasts a message to the entire server.");
        setAliases("announce", "bc", "ac");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Broadcasts a message to the entire server
     *
     *
     * Valid usages of command:
     * - /broadcast
     * - /bc
     *
     * Permissions used:
     * - ftc.commands.broadcast
     *
     * Author: Wout
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("announcement", StringArgumentType.greedyString())
                        .executes(context -> {
                            FtcCore.getAnnouncer().announce(context.getArgument("announcement", String.class));
                            return 0;
                        })
                )
                .then(argument("-component")
                        .then(argument("componentAnnouncement", ComponentArgument.component())
                                .executes(c -> {
                                    FtcCore.getAnnouncer().announce(c.getArgument("componentAnnouncement", Component.class));
                                    return 0;
                                })
                             )
                );
    }
}
