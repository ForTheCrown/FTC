package net.forthecrown.commands.marriage;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.actions.UserActionHandler;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.actions.MarriageMessage;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandMarriageChat extends FtcCommand {

    public CommandMarriageChat() {
        super("marriagechat", Crown.inst());

        setPermission(Permissions.MARRY);
        setAliases("marryc", "marriagec", "mc", "mchat");
        setDescription("Chat with your spouse privately");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     *
     * Permissions used:
     * ftc.marry
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("message", StringArgumentType.greedyString())
                        .suggests((c, b) -> FtcSuggestionProvider.suggestPlayerNamesAndEmotes(c, b, false))

                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            String str = c.getArgument("message", String.class);

                            UserInteractions inter = user.getInteractions();
                            if(inter.getSpouse() == null) throw FtcExceptionProvider.notMarried();

                            CrownUser spouse = UserManager.getUser(inter.getSpouse());
                            if(!spouse.isOnline()) throw UserArgument.USER_NOT_ONLINE.create(spouse.nickDisplayName());

                            MarriageMessage dm =  new MarriageMessage(user, spouse, str);
                            UserActionHandler.handleAction(dm);

                            return 0;
                        })
                );
    }
}