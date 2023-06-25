package net.forthecrown.waypoints;

import static net.forthecrown.Permissions.register;
import static net.forthecrown.Permissions.registerPrefixed;

import org.bukkit.permissions.Permission;

public interface WPermissions {

  Permission
      WAYPOINTS               = register("ftc.waypoints"),
      WAYPOINTS_ADMIN         = registerPrefixed(WAYPOINTS, "admin"),
      WAYPOINTS_FLAGS         = registerPrefixed(WAYPOINTS, "flags");

}