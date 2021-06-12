package net.forthecrown.emperor.commands.marriage;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserInteractions;
import net.forthecrown.emperor.user.UserManager;
import net.forthecrown.emperor.user.data.MarriageMessage;
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