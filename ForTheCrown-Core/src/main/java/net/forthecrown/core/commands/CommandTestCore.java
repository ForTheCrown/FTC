package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class CommandTestCore extends CrownCommandBuilder {

    public CommandTestCore(){
        super("coretest", FtcCore.getInstance());

        setPermission("ftc.admin.test");
        register();
    }

    @Override
    public boolean test(CommandListenerWrapper sender) { //test method used by Brigadier to determine who can use the command, from Predicate interface
        return sender.getBukkitSender().isOp() && testPermissionSilent(sender.getBukkitSender());
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
       command.executes(c -> {
           CrownUser u = getUserSender(c);
           u.sendMessage("-Beginning test-");
           //Use this command to test things lol
           //This is as close as I currently know how to get to actual automatic tests

           u.sendMessage("-Test finished-");
           return 0;
       });
    }
}
