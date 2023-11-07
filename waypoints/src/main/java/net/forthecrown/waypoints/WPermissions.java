package net.forthecrown.waypoints;

import static net.forthecrown.Permissions.register;
import static net.forthecrown.Permissions.register;

import net.forthecrown.Permissions;
import org.bukkit.permissions.Permission;

public interface WPermissions {

  Permission
      WAYPOINTS               = register("ftc.waypoints"),
      WAYPOINTS_ADMIN         = Permissions.register(WAYPOINTS, "admin"),
      WAYPOINTS_FLAGS         = Permissions.register(WAYPOINTS, "flags");

}