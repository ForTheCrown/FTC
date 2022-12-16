package net.forthecrown.commands.waypoint;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.royalgrenadier.types.selector.EntityArgumentImpl;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import org.bukkit.Sound;

public class CommandInvite extends FtcCommand {

    public CommandInvite() {
        super("Invite");

        setDescription("Invites a user to your waypoint");
        setPermission(Permissions.WAYPOINTS);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Invite
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("users", Arguments.ONLINE_USERS)
                        .executes(c -> {
                            var user = getUserSender(c);

                            if (!user.get(Properties.REGION_INVITING)) {
                                throw Exceptions.format("You have inviting turned off");
                            }

                            var waypoint = user.getHomes().getHomeTeleport();

                            if (waypoint == null) {
                                throw Exceptions.NO_HOME_REGION;
                            }

                            var targets = Arguments.getUsers(c, "user");
                            if (targets.size() == 1) {
                                User target = targets.get(0);

                                if (Users.testBlocked(user, target,
                                        Messages.DM_BLOCKED_SENDER,
                                        Messages.DM_BLOCKED_TARGET
                                )) {
                                    return 0;
                                }

                                if (!target.get(Properties.REGION_INVITING)) {
                                    throw Exceptions.format(
                                            "{0, user} doesn't accept region invites",
                                            target
                                    );
                                }
                            } else {
                                targets.removeIf(u -> {
                                    if (Users.areBlocked(u, user)) {
                                        return true;
                                    }

                                    return u.get(Properties.REGION_INVITING);
                                });

                                boolean selfRemoved = targets.remove(user);

                                if (targets.isEmpty()) {
                                    if (selfRemoved) {
                                        throw Exceptions.CANNOT_INVITE_SELF;
                                    } else {
                                        throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
                                    }
                                }
                            }

                            for (var target: targets) {
                                waypoint.invite(user.getUniqueId(), target.getUniqueId());

                                target.sendMessage(Messages.targetInvited(user));
                                target.playSound(Sound.UI_TOAST_IN, 2, 1.3f);
                                user.sendMessage(Messages.senderInvited(target));
                            }

                            if (targets.size() > 1) {
                                user.sendMessage(Messages.invitedTotal(targets.size()));
                            }

                            user.playSound(Sound.UI_TOAST_OUT, 2, 1.5f);

                            return 0;
                        })
                );
    }
}