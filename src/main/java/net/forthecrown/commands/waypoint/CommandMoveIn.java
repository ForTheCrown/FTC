package net.forthecrown.commands.waypoint;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.Waypoints;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class CommandMoveIn extends FtcCommand {

    public CommandMoveIn() {
        super("MoveIn");

        setPermission(Permissions.WAYPOINTS);
        setDescription("Sets your home waypoint");
        setAliases("sethomepole", "sethomepost");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /MoveIn
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    User user = getUserSender(c);
                    Waypoint waypoint = Waypoints.getNearest(user);

                    Waypoints.validateMoveInCooldown(user);

                    if (waypoint == null
                            || !waypoint.getBounds().contains(user.getPlayer())
                    ) {
                        var target = user.getPlayer().getTargetBlock(5);

                        if (target != null && Waypoints.isTopOfWaypoint(target)) {
                            Waypoints.tryCreate(c.getSource());

                            c.getSource().sendMessage(
                                    Messages.HOME_WAYPOINT_SET
                            );
                            return 0;
                        }

                        if (waypoint != null) {
                            throw Exceptions.farFromWaypoint(waypoint);
                        } else {
                            throw Exceptions.FAR_FROM_WAYPOINT;
                        }
                    }

                    user.getHomes().setHomeWaypoint(waypoint);
                    user.sendMessage(Messages.HOME_WAYPOINT_SET);
                    user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);

                    return 0;
                });
    }
}