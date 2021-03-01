package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class BroadcastCommand extends CrownCommandBuilder {

    public BroadcastCommand(){
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
     * - ftc.admin
     *
     * Referenced other classes:
     * - FtcCore: FtcCore.getPrefix
     *
     * Author: Wout
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.then(argument("announcement", StringArgumentType.greedyString())
                .executes(context -> {
                    FtcCore.getAnnouncer().announceToAll(context.getArgument("announcement", String.class));
                    return 0;
                })
        );
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        if(args.length < 1) return false;

        FtcCore.getAnnouncer().announceToAll(String.join(" ", args));
        return true;
    }*/
}
