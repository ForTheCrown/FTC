package net.forthecrown.commands.regions;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Messages;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;

public class CommandCancelInvite extends FtcCommand {

    public CommandCancelInvite() {
        super("cancelinvite");

        setPermission(Permissions.REGIONS);
        setDescription("Cancels a region invite");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Cancels an invite sent to a user for a
     * region visit
     *
     * Valid usages of command:
     * /cancelinvite <user>
     *
     * Permissions used: ftc.regions
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", Arguments.USER)
                        .executes(c -> {
                            User user = getUserSender(c);
                            User target = Arguments.getUser(c, "user");

                            if(!user.getInteractions().hasInvited(target.getUniqueId())) {
                                throw Exceptions.inviteNotSent(target);
                            }

                            user.getInteractions().removeInvite(target.getUniqueId());
                            target.getInteractions().removeRecievedInvite(user.getUniqueId());

                            user.sendMessage(Messages.inviteCancelled(target));
                            return 0;
                        })
                );
    }
}