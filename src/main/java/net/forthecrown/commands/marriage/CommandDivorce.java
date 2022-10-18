package net.forthecrown.commands.marriage;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Messages;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserInteractions;

public class CommandDivorce extends FtcCommand {

    public CommandDivorce() {
        super("divorce");

        setPermission(Permissions.MARRY);
        setDescription("Divorce your spouse");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /divorce
     *
     * Permissions used:
     * ftc.marry
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    UserInteractions inter = user.getInteractions();

                    testCanDivorce(user);

                    user.sendMessage(Messages.confirmDivorce(inter.spouseUser()));
                    return 0;
                })

                .then(literal("confirm")
                        .executes(c -> {
                            User user = getUserSender(c);

                            testCanDivorce(user);

                            user.getInteractions().divorce();
                            return 0;
                        })
                );
    }

    public static void testCanDivorce(User user) throws CommandSyntaxException {
        UserInteractions inter = user.getInteractions();

        if (inter.getSpouse() == null) {
            throw Exceptions.NOT_MARRIED;
        }

        if (!inter.canChangeMarriageStatus()) {
            throw Exceptions.marriageStatusSender(user);
        }

        User spouse = Users.get(inter.getSpouse());

        if(!spouse.getInteractions().canChangeMarriageStatus()) {
            throw Exceptions.marriageStatusTarget(spouse);
        }
    }
}