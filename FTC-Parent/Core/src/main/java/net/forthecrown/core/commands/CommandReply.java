package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.data.DirectMessage;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.royalgrenadier.source.CommandSources;

public class CommandReply extends FtcCommand {
    public CommandReply(){
        super("reply", CrownCore.inst());

        setPermission(Permissions.MESSAGE);
        setAliases("er", "ereply", "respond", "r");
        setDescription("Send a message to the last person to send you a message");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("message", StringArgumentType.greedyString())
                .suggests((c, b) -> CrownUtils.suggestPlayernamesAndEmotes(c, b, false))

                .executes(c -> {
                    CrownUser user = getUserSender(c);

                    CommandSource source = user.getLastMessage();
                    if(source == null || !sourceIsOnline(source)) throw FtcExceptionProvider.noReplyTargets();

                    new DirectMessage(CommandSources.getOrCreate(user.getPlayer(), this), source, true, c.getArgument("message", String.class))
                            .complete();
                    return 0;
                })
        );
    }

    private boolean sourceIsOnline(CommandSource source) throws CommandSyntaxException {
        if(!source.isPlayer()) return true;
        return source.asPlayer().isOnline();
    }
}
