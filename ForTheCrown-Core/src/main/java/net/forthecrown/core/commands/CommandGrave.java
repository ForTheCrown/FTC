package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.Grave;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class CommandGrave extends CrownCommandBuilder {

    public CommandGrave(){
        super("grave", FtcCore.getInstance());

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            Grave grave = user.getGrave();

            if(grave.isEmpty()) throw new CrownCommandException("&7[FTC] Your grave is empty!");
            grave.giveItems();
            return 0;
        });
    }
}
