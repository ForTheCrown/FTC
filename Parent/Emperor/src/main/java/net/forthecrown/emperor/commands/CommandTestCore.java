package net.forthecrown.emperor.commands;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import net.forthecrown.emperor.Announcer;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public class CommandTestCore extends CrownCommandBuilder {

    public CommandTestCore(){
        super("coretest", CrownCore.inst());

        setAliases("testcore");
        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    private final Timing timing = Timings.of(CrownCore.inst(), "putTimer");
    private final Timing timer = Timings.of(CrownCore.inst(), "getTimer");

    @Override
    public boolean test(CommandSource sender) { //test method used by Brigadier to determine who can use the command, from Predicate interface
        return sender.asBukkit().isOp() && testPermissionSilent(sender.asBukkit());
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser u = getUserSender(c);
            u.sendMessage("-Beginning test-");
            //Use this command to test things lol
            //This is as close as I currently know how to get to actual automatic test

            TranslatableComponent trans = Component.translatable("ftc.translations.test");
            TranslatableComponent trans_i_am = Component.translatable("ftc.translations.test.args", u.nickDisplayName());

            u.sendMessage(trans);
            u.sendMessage(trans_i_am);

            Announcer.debug(CrownCore.getMessages().getRegistry());
            u.sendMessage("-Test finished-");
            return 0;
        });
    }
}
