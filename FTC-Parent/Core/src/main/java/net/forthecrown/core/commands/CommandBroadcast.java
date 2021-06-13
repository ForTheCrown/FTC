package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.text.Component;

public class CommandBroadcast extends FtcCommand {

    public CommandBroadcast(){
        super("broadcast", CrownCore.inst());

        setDescription("Broadcasts a message to the entire server.");
        setAliases("announce", "bc", "ac");
        setPermission(Permissions.BROADCAST);
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
                            CrownCore.getAnnouncer().announce(context.getArgument("announcement", String.class));
                            return 0;
                        })
                )
                .then(literal("-component")
                        .then(argument("componentAnnouncement", ComponentArgument.component())
                                .executes(c -> {
                                    CrownCore.getAnnouncer().announce(c.getArgument("componentAnnouncement", Component.class));
                                    return 0;
                                })
                             )
                );
    }
}
