package net.forthecrown.commands.waypoint;

import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.waypoint.visit.WaypointVisit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class CommandVisit extends FtcCommand {

    public CommandVisit() {
        super("Visit");

        setAliases("v", "vr", "visitregion");
        setDescription("Visits a teleport waypoint");
        setPermission(Permissions.WAYPOINTS);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Visit
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("waypoint", Arguments.WAYPOINT)
                        .executes(c -> {
                            Cooldown.testAndThrow(c.getSource(), "waypoint_visit", 5 * 20);

                            var waypoint = Arguments.getWaypoint(c, "waypoint");
                            WaypointVisit.visit(getUserSender(c), waypoint);

                            return 0;
                        })
                );
    }
}