package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.DirectMessage;
import net.forthecrown.user.actions.UserActionHandler;

public class CommandReply extends FtcCommand {
    public CommandReply(){
        super("reply", Crown.inst());

        setPermission(Permissions.MESSAGE);
        setAliases("er", "ereply", "respond", "r");
        setDescription("Send a message to the last person to send you a message");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("message", StringArgumentType.greedyString())
                .suggests((c, b) -> FtcSuggestionProvider.suggestPlayerNamesAndEmotes(c, b, false))

                .executes(c -> {
                    CrownUser user = getUserSender(c);

                    CommandSource source = user.getLastMessage();
                    if(source == null || !sourceIsOnline(source)) throw FtcExceptionProvider.noReplyTargets();

                    UserActionHandler.handleAction(
                            new DirectMessage(GrenadierUtils.wrap(GrenadierUtils.senderToWrapper(user.getPlayer()), this), source, true, c.getArgument("message", String.class))
                    );
                    return 0;
                })
        );
    }

    private boolean sourceIsOnline(CommandSource source) throws CommandSyntaxException {
        if(!source.isPlayer()) return true;
        return source.asPlayer().isOnline();
    }
}
