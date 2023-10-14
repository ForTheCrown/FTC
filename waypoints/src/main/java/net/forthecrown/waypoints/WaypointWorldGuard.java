package net.forthecrown.waypoints;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.forthecrown.user.User;
import org.bukkit.block.Block;

public final class WaypointWorldGuard {
  private WaypointWorldGuard() {}

  public static final StateFlag CREATE_WAYPOINTS = new StateFlag("waypoint-creation", true);

  static void registerAll() {
    FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    registry.register(CREATE_WAYPOINTS);
  }

  public static boolean canCreateAt(Block block, User user) {
    if (user.hasPermission(WPermissions.WAYPOINTS_ADMIN)) {
      return true;
    }

    return WorldGuard.getInstance()
        .getPlatform()
        .getRegionContainer()
        .createQuery()
        .testState(
            BukkitAdapter.adapt(block.getLocation()),
            WorldGuardPlugin.inst().wrapPlayer(user.getPlayer()),
            CREATE_WAYPOINTS
        );
  }
}
