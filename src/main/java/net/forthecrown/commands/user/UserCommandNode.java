package net.forthecrown.commands.user;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import org.jetbrains.annotations.NotNull;

abstract class UserCommandNode extends FtcCommand {
    final String argumentName;

    public UserCommandNode(@NotNull String name, String argumentName) {
        super(name);
        this.argumentName = argumentName;

        setPermission(UserCommands.PERMISSION);
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        var argument = argument(UserCommands.USER_ARG_NAME, Arguments.USER);
        create(argument, c -> Arguments.getUser(c, UserCommands.USER_ARG_NAME));

        command.then(argument);
    }

    protected abstract <T extends ArgumentBuilder<CommandSource, T>> void create(T command, UserProvider provider);
}