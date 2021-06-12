package net.forthecrown.emperor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.user.data.DirectMessage;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.royalgrenadier.source.CommandSources;

public class CommandTell extends FtcCommand {
    public CommandTell(){
        super("ftell", CrownCore.inst());

        setAliases("emsg", "tell", "whisper", "w", "msg", "etell", "ewhisper", "pm", "dm", "t", "message");
        setPermission(Permissions.MESSAGE);
        setDescription("Sends a message to a player");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.
                then(argument("user", UserType.onlineUser())
                        .then(argument("message", StringArgumentType.greedyString())
                                .suggests(CrownUtils::suggestPlayernamesAndEmotes)

                                .executes(c -> {
                                    CommandSource source = c.getSource();
                                    CrownUser user = UserType.getUser(c, "user");
                                    String text = c.getArgument("message", String.class);

                                    return sendMsg(source, user, text);
                                })
                        )
                );
    }

    public int sendMsg(CommandSource from, CrownUser to, String message) throws CommandSyntaxException {
        CommandSource receiver = CommandSources.getOrCreate(to.getPlayer(), this);

        if(from.isPlayer()){
            CrownUser user = UserManager.getUser(from.asPlayer());
            if(to.getInteractions().isBlockedPlayer(user.getUniqueId())) throw FtcExceptionProvider.blockedPlayer(to);
            if(!user.equals(to)){
                user.setLastMessage(receiver);
                to.setLastMessage(from);
            }
        } else to.setLastMessage(from);
        new DirectMessage(from, receiver, false, message).complete();
        return 0;
    }
}