package net.forthecrown.commands.manager;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import org.jetbrains.annotations.NotNull;

public abstract class FtcCommand extends AbstractCommand {

    @Getter @Setter
    private String helpListName;

    protected FtcCommand(@NotNull String name) {
        super(name, FTC.getPlugin());

        // unknown command for permission message cuz you
        // don't need to know what kinds of commands we have
        permissionMessage(Messages.UNKNOWN_COMMAND);

        setPermission(Permissions.COMMAND_PREFIX + getName());
        setDescription("An FTC command");

        Commands.BY_NAME.put(getName(), this);
    }

    protected static User getUserSender(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return Users.get(c.getSource().asPlayer());
    }
}