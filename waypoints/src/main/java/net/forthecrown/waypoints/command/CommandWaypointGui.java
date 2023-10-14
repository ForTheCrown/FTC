package net.forthecrown.waypoints.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import net.forthecrown.command.Commands;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.user.User;
import net.forthecrown.waypoints.WPermissions;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.menu.WaypointMenus;
import org.jetbrains.annotations.Nullable;

public class CommandWaypointGui extends FtcCommand {

  public CommandWaypointGui() {
    super("waypointgui");

    setPermission(WPermissions.WAYPOINTS);
    setDescription("Opens the Waypoint GUI");

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> openMenu(c.getSource(), null, false))

        .then(literal("residents")
            .then(argument("uuid", ArgumentTypes.uuid())
                .executes(c -> {
                  UUID id = c.getArgument("uuid", UUID.class);
                  return openMenu(c.getSource(), id, true);
                })
            )
        )

        .then(argument("uuid", ArgumentTypes.uuid())
            .executes(c -> {
              UUID id = c.getArgument("uuid", UUID.class);
              return openMenu(c.getSource(), id, false);
            })
        );
  }

  private int openMenu(CommandSource source, @Nullable UUID uuid, boolean residents)
      throws CommandSyntaxException
  {
    User user = Commands.getUserSender(source);

    WaypointManager manager = WaypointManager.getInstance();
    Waypoint waypoint = uuid == null ? null : manager.get(uuid);

    MenuPage open;

    if (waypoint == null) {
      open = WaypointMenus.LIST_PAGE;
    } else {
      if (residents) {
        open = WaypointMenus.RESIDENTS_LIST;
      } else if (!waypoint.canEdit(user)) {
        open = WaypointMenus.NO_PERMS;
      } else {
        open = WaypointMenus.EDIT_MENU;
      }
    }

    WaypointMenus.open(open, user, waypoint);
    return 0;
  }
}
