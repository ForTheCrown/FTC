package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.InviteToRegion;
import net.forthecrown.user.actions.UserActionHandler;

import java.util.Collection;

public class CommandInvite extends FtcCommand {

    public CommandInvite() {
        super("invite");

        setPermission(Permissions.REGIONS);
        setDescription("Invites a player to your region");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Invites users to your region
     *
     * Valid usages of command:
     * /Invite
     *
     * Permissions used:
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("users", UserArgument.users())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            Collection<CrownUser> users = UserArgument.getUsers(c, "users");

                            users.remove(user);
                            if(users.isEmpty()) throw UserArgument.NO_USERS_FOUND.create();
                            UserActionHandler handler = Crown.getUserManager().getActionHandler();

                            //Invite all of them
                            for (CrownUser u: users) {
                                handler.handle(new InviteToRegion(user, u));
                            }

                            return 0;
                        })
                );
    }
}