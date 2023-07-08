package net.forthecrown;

import net.forthecrown.command.Commands;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public final class Permissions {
  private Permissions() {}

  /**
   * General admin permission
   * @deprecated Too broad, use permissions specific to use case
   */
  @Deprecated
  public static final Permission ADMIN      = register("ftc.admin");

  public static final Permission DEFAULT    = register("ftc.default");

  public static final Permission VANISH     = register("ftc.vanish");

  public static final Permission VANISH_SEE = register(VANISH.getName() + ".see");

  public static final Permission HELP       = registerCmd("help");

  public static final Permission WORLD_BYPASS = register("ftc.worldbypass");

  public static Permission register(String key) {
    var manager = Bukkit.getPluginManager();

    Permission permission = manager.getPermission(key);

    if (permission == null) {
      permission = new Permission(key);
      manager.addPermission(permission);
    }

    return permission;
  }

  public static Permission registerCmd(String name) {
    return register(Commands.DEFAULT_PERMISSION_FORMAT.replace("{command}", name));
  }

  public static Permission registerPrefixed(Permission parent, String suffix) {
    return register(parent.getName() + "." + suffix);
  }
}