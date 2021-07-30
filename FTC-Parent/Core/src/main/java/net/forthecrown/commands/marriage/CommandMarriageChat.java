package net.forthecrown.commands.marriage;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.arguments.UserType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.data.MarriageMessage;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandMarriageChat extends FtcCommand {

    public CommandMarriageChat() {
        super("marriagechat", ForTheCrown.inst());

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
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("message", StringArgumentType.greedyString())
                        .suggests((c, b) -> FtcSuggestionProvider.suggestPlayernamesAndEmotes(c, b, false))

                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            String str = c.getArgument("message", String.class);

                            UserInteractions inter = user.getInteractions();
                            if(inter.getMarriedTo() == null) throw FtcExceptionProvider.notMarried();

                            CrownUser spouse = UserManager.getUser(inter.getMarriedTo());
                            if(!spouse.isOnline()) throw UserType.USER_NOT_ONLINE.create(spouse.nickDisplayName());

                            new MarriageMessage(user, spouse, str)
                                    .complete();
                            return 0;
                        })
                );
    }
}