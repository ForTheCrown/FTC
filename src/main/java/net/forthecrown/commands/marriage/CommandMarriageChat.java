package net.forthecrown.commands.marriage;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.arguments.chat.MessageArgument;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.MarriageMessage;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserInteractions;

public class CommandMarriageChat extends FtcCommand {

    public CommandMarriageChat() {
        super("marriagechat");

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
                .then(argument("message", Arguments.MESSAGE)

                        .executes(c -> {
                            User user = getUserSender(c);
                            String str = c.getArgument("message", MessageArgument.Result.class)
                                    .getText();

                            UserInteractions inter = user.getInteractions();

                            if (inter.getSpouse() == null) {
                                throw Exceptions.NOT_MARRIED;
                            }

                            User spouse = Users.get(inter.getSpouse());

                            if (!spouse.isOnline()) {
                                throw Exceptions.notOnline(spouse);
                            }

                            MarriageMessage.send(user, spouse, str, false);
                            return 0;
                        })
                );
    }
}