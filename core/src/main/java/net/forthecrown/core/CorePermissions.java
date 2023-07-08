package net.forthecrown.core;

import static net.forthecrown.Permissions.registerCmd;
import static net.forthecrown.Permissions.registerPrefixed;

import org.bukkit.permissions.Permission;

public interface CorePermissions {

  Permission IGNORE                  = registerCmd("ignore");
  Permission IGNORELIST              = registerCmd("ignorelist");
  Permission IGNORELIST_OTHERS       = registerPrefixed(IGNORELIST, "others");
  Permission IGNORE_AC               = registerCmd("ignoreac");
}
