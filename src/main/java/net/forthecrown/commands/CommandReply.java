package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.chat.MessageArgument;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.DirectMessage;
import net.forthecrown.user.User;

public class CommandReply extends FtcCommand {
    public CommandReply(){
        super("reply");

        setPermission(Permissions.MESSAGE);
        setAliases("er", "ereply", "respond", "r");
        setDescription("Send a message to the last person to send you a message");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("message", Arguments.MESSAGE)
                .executes(c -> {
                    User user = getUserSender(c);
                    CommandSource source = user.getLastMessage();

                    if (source == null || !sourceIsOnline(source)) {
                        throw Exceptions.NO_REPLY_TARGETS;
                    }

                    DirectMessage.run(
                            CommandSource.of(user.getPlayer(), this),
                            source,
                            true,
                            c.getArgument("message", MessageArgument.Result.class).getText()
                    );
                    return 0;
                })
        );
    }

    private boolean sourceIsOnline(CommandSource source) throws CommandSyntaxException {
        if(!source.isPlayer()) {
            return true;
        }

        return source.asPlayer().isOnline();
    }
}