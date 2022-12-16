package net.forthecrown.commands.waypoint;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.waypoint.Waypoints;
import net.forthecrown.waypoint.type.WaypointTypes;

public class CommandCreateWaypoint extends FtcCommand {

    public CommandCreateWaypoint() {
        super("CreateWaypoint");

        setPermission(Permissions.WAYPOINTS);
        setDescription("Creates a new waypoint");
        setAliases("waypointcreate");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /CreateWaypoint
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Waypoints.tryCreate(c.getSource());
                    return 0;
                })

                .then(literal("-admin")
                        .requires(source -> source.hasPermission(Permissions.WAYPOINTS_ADMIN))
                        .executes(c -> {
                            Waypoints.makeWaypoint(
                                    WaypointTypes.ADMIN,
                                    null,
                                    c.getSource()
                            );
                            return 0;
                        })
                )

                .then(literal("-region_pole")
                        .requires(source -> source.hasPermission(Permissions.WAYPOINTS_ADMIN))

                        .executes(c -> {
                            Waypoints.makeWaypoint(
                                    WaypointTypes.REGION_POLE,
                                    null,
                                    c.getSource()
                            );
                            return 0;
                        })
                );
    }
}