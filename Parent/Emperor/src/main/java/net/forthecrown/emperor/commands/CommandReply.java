package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.DirectMessage;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.royalgrenadier.source.CommandSources;

public class CommandReply extends CrownCommandBuilder {
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
                .suggests(CrownUtils::suggeestPlayernamesAndEmotes)

                .executes(c -> {
                    CrownUser user = getUserSender(c);

                    CommandSource source = user.getLastMessage();
                    if(source == null) throw FtcExceptionProvider.noReplyTargets();

                    new DirectMessage(CommandSources.getOrCreate(user.getPlayer(), this), source, true, c.getArgument("message", String.class))
                            .complete();
                    return 0;
                })
        );
    }
}
