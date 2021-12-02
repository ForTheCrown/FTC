package net.forthecrown.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.UserActionHandler;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.actions.DirectMessage;
import net.forthecrown.commands.manager.FtcSuggestionProvider;

public class CommandTell extends FtcCommand {
    public CommandTell(){
        super("ftell", Crown.inst());

        setAliases("emsg", "tell", "whisper", "w", "msg", "etell", "ewhisper", "pm", "dm", "t", "message");
        setPermission(Permissions.MESSAGE);
        setDescription("Sends a message to a player");
        setHelpListName("msg");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.
                then(argument("user", UserArgument.onlineUser())
                        .then(argument("message", StringArgumentType.greedyString())
                                .suggests((c, b) -> FtcSuggestionProvider.suggestPlayerNamesAndEmotes(c, b, false))

                                .executes(c -> {
                                    CommandSource source = c.getSource();
                                    CrownUser user = UserArgument.getUser(c, "user");
                                    String text = c.getArgument("message", String.class);

                                    return sendMsg(source, user, text);
                                })
                        )
                );
    }

    public int sendMsg(CommandSource from, CrownUser to, String message) throws CommandSyntaxException {
        CommandSource receiver = to.getCommandSource(this);

        if(from.isPlayer()){
            CrownUser user = UserManager.getUser(from.asPlayer());
            if(to.getInteractions().isBlockedPlayer(user.getUniqueId())) throw FtcExceptionProvider.blockedPlayer(to);
            if(!user.equals(to)){
                user.setLastMessage(receiver);
                to.setLastMessage(from);
            }
        } else to.setLastMessage(from);


        DirectMessage dm = new DirectMessage(from, receiver, false, message);
        UserActionHandler.handleAction(dm);

        return 0;
    }
}