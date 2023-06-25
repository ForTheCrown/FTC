package net.forthecrown.waypoints.command;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoints;
import net.forthecrown.waypoints.type.WaypointTypes;

public class CommandCreateWaypoint extends FtcCommand {

  public CommandCreateWaypoint() {
    super("CreateWaypoint");

    setPermission(WPermissions.WAYPOINTS_ADMIN);
    setDescription("Creates a new waypoint");
    setAliases("waypointcreate");
    simpleUsages();

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
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Attempts to create a waypoint from the block")
        .addInfo("you're looking at");

    factory.usage("-admin")
        .addInfo("Creates an invisible admin waypoint")
        .addInfo("where you're standing");

    factory.usage("-region_pole")
        .addInfo("Creates a region pole waypoint from the block")
        .addInfo("you're looking at");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          Waypoints.tryCreate(c.getSource());
          return 0;
        })

        .then(literal("-admin")
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