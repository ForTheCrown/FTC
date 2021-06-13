package net.forthecrown.core.commands.marriage;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.commands.arguments.UserType;
import net.forthecrown.core.commands.manager.FtcCommand;
import net.forthecrown.core.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserInteractions;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.core.user.data.MarriageMessage;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class CommandMarriageChat extends FtcCommand {

    public CommandMarriageChat() {
        super("marriagechat", CrownCore.inst());

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